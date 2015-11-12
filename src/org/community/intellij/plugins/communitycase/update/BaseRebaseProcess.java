/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.update;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.changes.shelf.ShelveChangesManager;
import com.intellij.openapi.vcs.changes.shelf.ShelvedChangeList;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.changes.ChangeUtils;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.HandlerUtil;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.commands.LineHandlerAdapter;
import org.community.intellij.plugins.communitycase.config.VcsSettings;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.rebase.RebaseUtils;
import org.community.intellij.plugins.communitycase.ui.UiUtil;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract class that implement rebase operation for several roots based on rebase operation (for example update operation)
 */
public abstract class BaseRebaseProcess {
  /**
   * The logger
   */
  private static final Logger LOG = Logger.getInstance("#"+BaseRebaseProcess.class.getName());
  /**
   * The context project
   */
  protected Project myProject;
  /**
   * The vcs service
   */
  protected Vcs myVcs;
  /**
   * The exception list
   */
  protected List<VcsException> myExceptions;
  /**
   * Copy of local change list
   */
  private List<LocalChangeList> myListsCopy;
  /**
   * The changes sorted by root
   */
  private final Map<VirtualFile, List<Change>> mySortedChanges = new HashMap<VirtualFile, List<Change>>();
  /**
   * The change list manager
   */
  private final ChangeListManagerEx myChangeManager;
  /**
   * Roots to stash
   */
  private final HashSet<VirtualFile> myRootsToStash = new HashSet<VirtualFile>();
  /**
   * True if the stash was created (root local variable)
   */
  private boolean stashCreated;
  /**
   * The stash message
   */
  private String myStashMessage;
  /**
   * Shelve manager instance
   */
  private ShelveChangesManager myShelveManager;
  /**
   * The shelved change list (used when {@code SHELVE} policy is selected)
   */
  private ShelvedChangeList myShelvedChangeList;
  /**
   * Contains vcs roots for which commits were skipped
   */
  private SortedMap<VirtualFile, List<RebaseUtils.CommitInfo>> mySkippedCommits =
    new TreeMap<VirtualFile, List<RebaseUtils.CommitInfo>>(Util.VIRTUAL_FILE_COMPARATOR);
  /**
   * The progress indicator to use
   */
  private ProgressIndicator myProgressIndicator;

  public BaseRebaseProcess(final Vcs vcs, final Project project, List<VcsException> exceptions) {
    myVcs = vcs;
    myProject = project;
    myExceptions = exceptions;
    myChangeManager = (ChangeListManagerEx)ChangeListManagerEx.getInstance(myProject);
  }

  /**
   * Perform rebase operation
   *
   * @param progressIndicator the progress indicator to use
   * @param roots             the vcs roots
   */
  public void doUpdate(ProgressIndicator progressIndicator, Set<VirtualFile> roots) {
    ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();
    projectManager.blockReloadingProjectOnExternalChanges();
    this.myProgressIndicator = progressIndicator;
    try {
      if (areRootsUnderRebase(roots)) return;
      if (!saveProjectChangesBeforeUpdate()) return;
      try {
        for (final VirtualFile root : roots) {
          List<RebaseUtils.CommitInfo> skippedCommits = null;
          try {
            // check if there is a remote for the branch
            final Branch branch = Branch.current(myProject, root);
            if (branch == null) {
              continue;
            }
            final String value = branch.getTrackedRemoteName(myProject, root);
            if (value == null || value.length() == 0) {
              continue;
            }
            final Ref<Boolean> cancelled = new Ref<Boolean>(false);
            final Ref<Throwable> ex = new Ref<Throwable>();
            saveRootChangesBeforeUpdate(root);
            boolean hadAbortErrors = false;
            try {
              markStart(root);
              try {
                LineHandler h = makeStartHandler(root);
                RebaseConflictDetector rebaseConflictDetector = new RebaseConflictDetector();
                h.addLineListener(rebaseConflictDetector);
                try {
                  HandlerUtil
                    .doSynchronouslyWithExceptions(h, progressIndicator, HandlerUtil.formatOperationName("Updating", root));
                }
                finally {
                  if (!rebaseConflictDetector.isRebaseConflict()) {
                    myExceptions.addAll(h.errors());
                  }
                  cleanupHandler(root, h);
                }
                while (rebaseConflictDetector.isRebaseConflict() && !cancelled.get() && !hadAbortErrors) {
                  mergeFiles(root, cancelled, ex, true);
                  //noinspection ThrowableResultOfMethodCallIgnored
                  if (ex.get() != null) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    throw Util.rethrowVcsException(ex.get());
                  }
                  checkLocallyModified(root, cancelled, ex);
                  //noinspection ThrowableResultOfMethodCallIgnored
                  if (ex.get() != null) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    throw Util.rethrowVcsException(ex.get());
                  }
                  if (cancelled.get()) {
                    break;
                  }
                  Collection<VcsException> exceptions = doRebase(progressIndicator, root, rebaseConflictDetector, "--continue");
                  while (rebaseConflictDetector.isNoChange() && !hasAbortExceptions(exceptions)) {
                    if (skippedCommits == null) {
                      skippedCommits = new ArrayList<RebaseUtils.CommitInfo>();
                      mySkippedCommits.put(root, skippedCommits);
                    }
                    skippedCommits.add(RebaseUtils.getCurrentRebaseCommit(root));
                    exceptions = doRebase(progressIndicator, root, rebaseConflictDetector, "--skip");
                  }
                  hadAbortErrors = hasAbortExceptions(exceptions);
                }
                if (cancelled.get() || hadAbortErrors) {
                  //noinspection ThrowableInstanceNeverThrown
                  myExceptions.add(new VcsException(
                    "The update process was " + (hadAbortErrors ? "aborted" : "cancelled") + " for " + root.getPresentableUrl()));
                  doRebase(progressIndicator, root, rebaseConflictDetector, "--abort");
                  progressIndicator.setText2("Refreshing files for the root " + root.getPath());
                  root.refresh(false, true);
                }
              }
              finally {
                markEnd(root, cancelled.get());
              }
            }
            finally {
              restoreRootChangesAfterUpdate(root, cancelled);
            }
          }
          catch (VcsException ex) {
            myExceptions.add(ex);
          }
        }
      }
      finally {
        restoreProjectChangesAfterUpdate();
      }
    }
    finally {
      projectManager.unblockReloadingProjectOnExternalChanges();
    }
  }

  /**
   * Check if the exceptions should cause an abort for the rebase process
   *
   * @param exceptions the exceptions to check (it should be result of single operation)
   * @return true if rebase process should be aborted
   */
  private static boolean hasAbortExceptions(Collection<VcsException> exceptions) {
    if (exceptions.size() > 1) {
      return true;
    }
    if (exceptions.size() == 1) {
      @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"}) final VcsException ex = exceptions.iterator().next();
      return !ex.getMessage().startsWith("Failed to merge in the changes");
    }
    return false;
  }

  /**
   * Restore project changes after update
   */
  private void restoreProjectChangesAfterUpdate() {
    if (mySkippedCommits.size() > 0) {
      SkippedCommits.showSkipped(myProject, mySkippedCommits);
    }
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.SHELVE) {
      if (myShelvedChangeList != null) {
        myProgressIndicator.setText(Bundle.getString("update.unshelving.changes"));
        //StashUtils.doSystemUnshelve(myProject, myShelvedChangeList, myShelveManager, myChangeManager, myExceptions);
        //todo wc unshelve changes here
      }
    }
    // Move files back to theirs change lists
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.SHELVE || getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH) {
      VcsDirtyScopeManager m = VcsDirtyScopeManager.getInstance(myProject);
      final boolean isStash = getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH;
      HashSet<File> filesToRefresh = isStash ? new HashSet<File>() : null;
      for (LocalChangeList changeList : myListsCopy) {
        for (Change c : changeList.getChanges()) {
          ContentRevision after = c.getAfterRevision();
          if (after != null) {
            m.fileDirty(after.getFile());
            if (isStash) {
              filesToRefresh.add(after.getFile().getIOFile());
            }
          }
          ContentRevision before = c.getBeforeRevision();
          if (before != null) {
            m.fileDirty(before.getFile());
            if (isStash) {
              filesToRefresh.add(before.getFile().getIOFile());
            }
          }
        }
      }
      if (isStash) {
        LocalFileSystem.getInstance().refreshIoFiles(filesToRefresh);
      }
      com.intellij.util.ui.UIUtil.invokeLaterIfNeeded(new Runnable() {
          public void run() {
              myChangeManager.invokeAfterUpdate(new Runnable() {
                  public void run() {
                      for (LocalChangeList changeList : myListsCopy) {
                          final Collection<Change> changes = changeList.getChanges();
                          if (!changes.isEmpty()) {
                              LOG.debug("After restoring files: moving " + changes.size() + " changes to '" + changeList.getName() + "'");
                              myChangeManager.moveChangesTo(changeList, changes.toArray(new Change[changes.size()]));
                          }
                      }
                  }
              }, InvokeAfterUpdateMode.BACKGROUND_NOT_CANCELLABLE, Bundle.getString("update.restoring.change.lists"),
                      ModalityState.NON_MODAL);
          }
      });
    }
  }

  /**
   * Restore per-root changes after update
   *
   * @param root      the just updated root
   * @param cancelled
   */
  private void restoreRootChangesAfterUpdate(VirtualFile root, Ref<Boolean> cancelled) {
    final Ref<Throwable> ex = new Ref<Throwable>();
    if (new File(root.getPath(), "MERGE_HEAD").exists()) {
      // in case of unfinished merge offer direct merging
      mergeFiles(root, cancelled, ex, false);
      //noinspection ThrowableResultOfMethodCallIgnored
      if (ex.get() != null) {
        //noinspection ThrowableResultOfMethodCallIgnored
        myExceptions.add(Util.rethrowVcsException(ex.get()));
      }
    }
    if (stashCreated && getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH) {
      myProgressIndicator.setText(HandlerUtil.formatOperationName("Unstashing changes to", root));
      //unstash(root);
      // after unstash, offer reverse merge
      mergeFiles(root, cancelled, ex, true);
      //noinspection ThrowableResultOfMethodCallIgnored
      if (ex.get() != null) {
        //noinspection ThrowableResultOfMethodCallIgnored
        myExceptions.add(Util.rethrowVcsException(ex.get()));
      }
    }

  }

  /**
   * Save per-root changes before update
   *
   * @param root the root to save changes for
   * @throws VcsException if there is a problem with saving changes
   */
  private void saveRootChangesBeforeUpdate(VirtualFile root) throws VcsException {
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH) {
      stashCreated = false;
      if (myRootsToStash.contains(root)) {
        myProgressIndicator.setText(HandlerUtil.formatOperationName("Stashing changes from", root));
        //stashCreated = StashUtils.saveStash(myProject, root, myStashMessage);
      }
    }
  }

  /**
   * Do the project level work required to save the changes
   *
   * @return false, if update process needs to be aborted
   */
  private boolean saveProjectChangesBeforeUpdate() {
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH || getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.SHELVE) {
      myStashMessage = makeStashMessage();
      myListsCopy = myChangeManager.getChangeListsCopy();
      for (LocalChangeList l : myListsCopy) {
        final Collection<Change> changeCollection = l.getChanges();
        LOG.debug("Stashing " + changeCollection.size() + " changes from '" + l.getName() + "'");
        for (Change c : changeCollection) {
          ContentRevision after = c.getAfterRevision();
          if (after != null) {
            VirtualFile r = Util.getRootOrNull(after.getFile());
            if (r != null) {
              myRootsToStash.add(r);
              List<Change> changes = mySortedChanges.get(r);
              if (changes == null) {
                changes = new ArrayList<Change>();
                mySortedChanges.put(r, changes);
              }
              changes.add(c);
            }
          }
          else {
            ContentRevision before = c.getBeforeRevision();
            if (before != null) {
              VirtualFile r = Util.getRootOrNull(before.getFile());
              if (r != null) {
                myRootsToStash.add(r);
              }
            }
          }
        }
      }
    }
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.STASH) {
      VcsSettings settings = VcsSettings.getInstance(myProject);
      if (settings == null) {
        return false;
      }
    }
    if (getUpdatePolicy() == VcsSettings.UpdateChangesPolicy.SHELVE) {
      myShelveManager = ShelveChangesManager.getInstance(myProject);
      ArrayList<Change> changes = new ArrayList<Change>();
      for (LocalChangeList l : myListsCopy) {
        changes.addAll(l.getChanges());
      }
      if (changes.size() > 0) {
        myProgressIndicator.setText(Bundle.getString("update.shelving.changes"));
        //myShelvedChangeList = StashUtils.shelveChanges(myProject, myShelveManager, changes, myStashMessage, myExceptions);
        //todo wc shelve changes here
        if (myShelvedChangeList == null) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Clean up the start handler
   *
   * @param root the root
   * @param h    the handler
   */
  @SuppressWarnings({"UnusedDeclaration"})
  protected void cleanupHandler(VirtualFile root, LineHandler h) {
    // do nothing by default
  }

  /**
   * Make handler that starts operation
   *
   * @param root the vcs root
   * @return the handler that starts rebase operation
   * @throws VcsException in if there is problem with running
   */
  protected abstract LineHandler makeStartHandler(VirtualFile root) throws VcsException;

  /**
   * Unstash changes and restore them in change list
   *
   * @param root the vcs root
   */
/*
  private void unstash(VirtualFile root) {
    try {
      StashUtils.popLastStash(myProject, root);
    }
    catch (final VcsException ue) {
      myExceptions.add(ue);
      com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
          public void run() {
              UiUtil.showOperationError(myProject, ue, "Auto-unstash");
          }
      });
    }
  }
*/

  /**
   * Mark the start of the operation
   *
   * @param root the vcs root
   * @throws VcsException the exception
   */
  protected void markStart(VirtualFile root) throws VcsException {

  }

  /**
   * Mark the end of the operation
   *
   * @param root      the vcs operation
   * @param cancelled true if the operation was cancelled due to update operation
   */
  protected void markEnd(VirtualFile root, boolean cancelled) {

  }

  /**
   * @return a stash message for the operation
   */
  protected abstract String makeStashMessage();

  /**
   * @return the policy of autosaving change
   */
  protected abstract VcsSettings.UpdateChangesPolicy getUpdatePolicy();

  /**
   * Check if some roots are under the rebase operation and show a message in this case
   *
   * @param roots the roots to check
   * @return true if some roots are being rebased
   */
  private boolean areRootsUnderRebase(Set<VirtualFile> roots) {
    Set<VirtualFile> rebasingRoots = new TreeSet<VirtualFile>(Util.VIRTUAL_FILE_COMPARATOR);
    for (final VirtualFile root : roots) {
      if (RebaseUtils.isRebaseInTheProgress(root)) {
        rebasingRoots.add(root);
      }
    }
    if (!rebasingRoots.isEmpty()) {
      final StringBuilder files = new StringBuilder();
      for (VirtualFile r : rebasingRoots) {
        files.append(Bundle.message("update.root.rebasing.item", r.getPresentableUrl()));
        //noinspection ThrowableInstanceNeverThrown
        myExceptions.add(new VcsException(Bundle.message("update.root.rebasing", r.getPresentableUrl())));
      }
      com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
          public void run() {
              Messages.showErrorDialog(myProject, Bundle.message("update.root.rebasing.message", files.toString()),
                      Bundle.message("update.root.rebasing.title"));
          }
      });
      return true;
    }
    return false;
  }

  /**
   * Merge files
   *
   * @param root      the project root
   * @param cancelled the cancelled indicator
   * @param ex        the exception holder
   * @param reverse   if true, reverse merge provider will be used
   */
  private void mergeFiles(final VirtualFile root, final Ref<Boolean> cancelled, final Ref<Throwable> ex, final boolean reverse) {
    com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
        public void run() {
            try {
                List<VirtualFile> affectedFiles = ChangeUtils.unmergedFiles(myProject, root);
                while (affectedFiles.size() != 0) {
                    AbstractVcsHelper.getInstance(myProject)
                            .showMergeDialog(affectedFiles, reverse ? myVcs.getReverseMergeProvider() : myVcs.getMergeProvider());
                    affectedFiles = ChangeUtils.unmergedFiles(myProject, root);
                    if (affectedFiles.size() != 0) {
                        int result = Messages.showYesNoDialog(myProject,
                                Bundle.message("update.rebase.unmerged",
                                        StringUtil.escapeXml(root.getPresentableUrl())),
                                Bundle.getString("update.rebase.unmerged.title"),
                                Messages.getErrorIcon());
                        if (result != 0) {
                            cancelled.set(true);
                            return;
                        }
                    }
                }
            } catch (Throwable t) {
                ex.set(t);
            }
        }
    });
  }

  /**
   * Check and process locally modified files
   *
   * @param root      the project root
   * @param cancelled the cancelled indicator
   * @param ex        the exception holder
   */
  private void checkLocallyModified(final VirtualFile root, final Ref<Boolean> cancelled, final Ref<Throwable> ex) {
    com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
        public void run() {
            try {
                if (!UpdateLocallyModifiedDialog.showIfNeeded(myProject, root)) {
                    cancelled.set(true);
                }
            } catch (Throwable t) {
                ex.set(t);
            }
        }
    });
  }

  /**
   * Do rebase operation as part of update operator
   *
   * @param progressIndicator      the progress indicator for the update
   * @param root                   the vcs root
   * @param rebaseConflictDetector the detector of conflicts in rebase operation
   * @param action                 the rebase action to execute
   * @return collected exceptions
   */
  private Collection<VcsException> doRebase(ProgressIndicator progressIndicator,
                                            VirtualFile root,
                                            RebaseConflictDetector rebaseConflictDetector,
                                            final String action) {
    LineHandler rh = new LineHandler(myProject, root, Command.REBASE);
    // ignore failure for abort
    rh.ignoreErrorCode(1);
    rh.addParameters(action);
    rebaseConflictDetector.reset();
    rh.addLineListener(rebaseConflictDetector);
    if (!"--abort".equals(action)) {
      configureRebaseEditor(root, rh);
    }
    try {
      return HandlerUtil.doSynchronouslyWithExceptions(rh, progressIndicator, HandlerUtil.formatOperationName("Rebasing ", root));
    }
    finally {
      cleanupHandler(root, rh);
    }
  }

  /**
   * Configure rebase editor
   *
   * @param root the vcs root
   * @param h    the handler to configure
   */
  @SuppressWarnings({"UnusedDeclaration"})
  protected void configureRebaseEditor(VirtualFile root, LineHandler h) {
    // do nothing by default
  }

  /**
   * The detector of conflict conditions for rebase operation
   */
  static class RebaseConflictDetector extends LineHandlerAdapter {
    /**
     * The line that indicates that there is a rebase conflict.
     */
    private final static String[] REBASE_CONFLICT_INDICATORS = {"When you have resolved this problem run \" rebase --continue\".",
      "Automatic cherry-pick failed.  After resolving the conflicts,"};
    /**
     * The line that indicates "no change" condition.
     */
    private static final String REBASE_NO_CHANGE_INDICATOR = "No changes - did you forget to use ' add'?";
    /**
     * if true, the rebase conflict happened
     */
    AtomicBoolean rebaseConflict = new AtomicBoolean(false);
    /**
     * if true, the no changes were detected in the rebase operations
     */
    AtomicBoolean noChange = new AtomicBoolean(false);

    /**
     * Reset detector before new operation
     */
    public void reset() {
      rebaseConflict.set(false);
      noChange.set(false);
    }

    /**
     * @return true if "no change" condition was detected during the operation
     */
    public boolean isNoChange() {
      return noChange.get();
    }

    /**
     * @return true if conflict during rebase was detected
     */
    public boolean isRebaseConflict() {
      return rebaseConflict.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLineAvailable(String line, Key outputType) {
      for (String i : REBASE_CONFLICT_INDICATORS) {
        if (line.startsWith(i)) {
          rebaseConflict.set(true);
          break;
        }
      }
      if (line.startsWith(REBASE_NO_CHANGE_INDICATOR)) {
        noChange.set(true);
      }
    }
  }
}
