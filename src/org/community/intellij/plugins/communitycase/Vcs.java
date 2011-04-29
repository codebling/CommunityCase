/*
 * Copyright 2000-2010 JetBrains s.r.o.
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
package org.community.intellij.plugins.communitycase;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.patch.formove.FilePathComparator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.CommitExecutor;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ComparatorDelegate;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.UIUtil;
import org.community.intellij.plugins.communitycase.annotate.IntellijAnnotationProvider;
import org.community.intellij.plugins.communitycase.changes.ChangeUtils;
import org.community.intellij.plugins.communitycase.changes.CommittedChangeListProvider;
import org.community.intellij.plugins.communitycase.changes.OutgoingChangesProvider;
import org.community.intellij.plugins.communitycase.checkin.CheckinEnvironment;
import org.community.intellij.plugins.communitycase.checkin.CommitAndPushExecutor;
import org.community.intellij.plugins.communitycase.checkout.branches.BranchConfigurations;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.config.*;
import org.community.intellij.plugins.communitycase.diff.TreeDiffProvider;
import org.community.intellij.plugins.communitycase.history.HistoryProvider;
import org.community.intellij.plugins.communitycase.history.NewUsersComponent;
import org.community.intellij.plugins.communitycase.history.browser.ProjectLogManager;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.merge.MergeProvider;
import org.community.intellij.plugins.communitycase.update.UpdateEnvironment;
import org.community.intellij.plugins.communitycase.vfs.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  VCS implementation
 */
public class Vcs extends AbstractVcs<CommittedChangeList> {
  public static final String NOTIFICATION_GROUP_ID = "ClearCase";
  public static final String NAME = "ClearCase"; // Vcs name

  //private static final Logger log = Logger.getInstance("#"+Vcs.class.getName());
  private static final VcsKey ourKey = createKey(NAME);

  private final com.intellij.openapi.vcs.changes.ChangeProvider myChangeProvider;
  private final CheckinEnvironment myCheckinEnvironment;
  private final com.intellij.openapi.vcs.rollback.RollbackEnvironment myRollbackEnvironment;
  private final UpdateEnvironment myUpdateEnvironment;
  private final IntellijAnnotationProvider myAnnotationProvider;
  private final com.intellij.openapi.vcs.diff.DiffProvider myDiffProvider;
  private final VcsHistoryProvider myHistoryProvider;
  private final com.intellij.openapi.vcs.EditFileProvider myEditFileProvider;
  private final ProjectLevelVcsManager myVcsManager;
  private final VcsSettings mySettings;
  private final Configurable myConfigurable;
  private final com.intellij.openapi.vcs.diff.RevisionSelector myRevSelector;
  private final MergeProvider myMergeProvider;
  private final MergeProvider myReverseMergeProvider;
  private final CommittedChangeListProvider myCommittedChangeListProvider;

  private VfsListener myVfsListener; // a VFS listener that tracks file addition, deletion, and renaming.
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private Version myVersion; // The currently detected  version or null.
  private final Object myCheckingVersion = new Object(); // Checking the version lock (used to prevent infinite recursion)
  private String myVersionCheckExcecutable = ""; // The path to executable at the time of version check

  private RootTracker myRootTracker; // The tracker that checks validity of  roots
  private final EventDispatcher<RootsListener> myRootListeners = EventDispatcher.create(RootsListener.class);
  private final EventDispatcher<ConfigListener> myConfigListeners = EventDispatcher.create(ConfigListener.class);
  private final EventDispatcher<ReferenceListener> myReferenceListeners = EventDispatcher.create(ReferenceListener.class);
  private IgnoreTracker myIgnoreTracker;
  private ConfigTracker myConfigTracker;
  private final BackgroundTaskQueue myTaskQueue; // The queue that is used to schedule background task from actions
  private final ReadWriteLock myCommandLock = new ReentrantReadWriteLock(true); // The command read/write lock
  private final TreeDiffProvider myTreeDiffProvider;
  private final CommitAndPushExecutor myCommitAndPushExecutor;
  private ReferenceTracker myReferenceTracker;
  private boolean isActivated; // If true, the vcs was activated
  private ExecutableValidator myExecutableValidator;
  private RepositoryChangeListener myIndexChangeListener;

  public static Vcs getInstance(@NotNull Project project) {
    return (Vcs)ProjectLevelVcsManager.getInstance(project).findVcsByName(NAME);
  }

  public Vcs(@NotNull Project project,
             @NotNull final org.community.intellij.plugins.communitycase.changes.ChangeProvider changeProvider,
             @NotNull final CheckinEnvironment checkinEnvironment,
             @NotNull final ProjectLevelVcsManager vcsManager,
             @NotNull final IntellijAnnotationProvider annotationProvider,
             @NotNull final org.community.intellij.plugins.communitycase.diff.DiffProvider diffProvider,
             @NotNull final HistoryProvider historyProvider,
             @NotNull final org.community.intellij.plugins.communitycase.rollback.RollbackEnvironment rollbackEnvironment,
             @NotNull final org.community.intellij.plugins.communitycase.edit.EditFileProvider editFileProvider,
             @NotNull final VcsSettings settings) {
    super(project, NAME);
    myVcsManager = vcsManager;
    mySettings = settings;
    myChangeProvider = changeProvider;
    myCheckinEnvironment = checkinEnvironment;
    myAnnotationProvider = annotationProvider;
    myDiffProvider = diffProvider;
    myHistoryProvider = historyProvider;
    myRollbackEnvironment = rollbackEnvironment;
    myEditFileProvider = editFileProvider;
    myRevSelector = new RevisionSelector();
    myConfigurable = new VcsConfigurable(settings, myProject);
    myUpdateEnvironment = new org.community.intellij.plugins.communitycase.update.UpdateEnvironment(myProject, this, settings);
    myMergeProvider = new org.community.intellij.plugins.communitycase.merge.MergeProvider(myProject);
    myReverseMergeProvider = new org.community.intellij.plugins.communitycase.merge.MergeProvider(myProject, true);
    myCommittedChangeListProvider = new CommittedChangeListProvider(myProject);
    myOutgoingChangesProvider = new OutgoingChangesProvider(myProject);
    myTreeDiffProvider = new TreeDiffProvider(myProject);
    myCommitAndPushExecutor = new CommitAndPushExecutor(checkinEnvironment);
    myReferenceTracker = new ReferenceTracker(myProject, this, myReferenceListeners.getMulticaster());
    myTaskQueue = new BackgroundTaskQueue(myProject, Bundle.getString("task.queue.title"));
    myIndexChangeListener = new RepositoryChangeListener(myProject, "./index");
  }

  /**
   * @return the vfs listener instance
   */
  public VfsListener getVfsListener() {
    return myVfsListener;
  }

  /**
   * @return the command lock
   */
  public ReadWriteLock getCommandLock() {
    return myCommandLock;
  }

  /**
   * Run task in background using the common queue (per project)
   *
   * @param task the task to run
   */
  public void runInBackground(Task.Backgroundable task) {
    myTaskQueue.run(task);
  }

  /**
   * Add listener for  roots
   *
   * @param listener the listener to add
   */
  public void addConfigListener(ConfigListener listener) {
    myConfigListeners.addListener(listener);
  }

  /**
   * Remove listener for  roots
   *
   * @param listener the listener to remove
   */
  public void removeConfigListener(ConfigListener listener) {
    myConfigListeners.removeListener(listener);
  }

  /**
   * Add listener for  roots
   *
   * @param listener the listener to add
   */
  public void addReferenceListener(ReferenceListener listener) {
    myReferenceListeners.addListener(listener);
  }

  /**
   * Remove listener for  roots
   *
   * @param listener the listener to remove
   */
  public void removeReferenceListener(ReferenceListener listener) {
    myReferenceListeners.removeListener(listener);
  }

  /**
   * Add listener for  roots
   *
   * @param listener the listener to add
   */
  public void addRootsListener(RootsListener listener) {
    myRootListeners.addListener(listener);
  }

  /**
   * Remove listener for  roots
   *
   * @param listener the listener to remove
   */
  public void removeRootsListener(RootsListener listener) {
    myRootListeners.removeListener(listener);
  }

  /**
   * @return a reverse merge provider for  (with reversed meaning of "theirs" and "yours", needed for the rebase and unstash)
   */
  @NotNull
  public com.intellij.openapi.vcs.merge.MergeProvider getReverseMergeProvider() {
    return myReverseMergeProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CommittedChangesProvider getCommittedChangesProvider() {
    return myCommittedChangeListProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getRevisionPattern() {
    // return the full commit hash pattern, possibly other revision formats should be supported as well
    return "[0-9a-fA-F]{40}";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public CheckinEnvironment getCheckinEnvironment() {
    return myCheckinEnvironment;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public com.intellij.openapi.vcs.merge.MergeProvider getMergeProvider() {
    return myMergeProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public com.intellij.openapi.vcs.rollback.RollbackEnvironment getRollbackEnvironment() {
    return myRollbackEnvironment;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public VcsHistoryProvider getVcsHistoryProvider() {
    return myHistoryProvider;
  }

  @Override
  public VcsHistoryProvider getVcsBlockHistoryProvider() {
    return myHistoryProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public String getDisplayName() {
    return NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public com.intellij.openapi.vcs.update.UpdateEnvironment getUpdateEnvironment() {
    return myUpdateEnvironment;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public IntellijAnnotationProvider getAnnotationProvider() {
    return myAnnotationProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  public com.intellij.openapi.vcs.diff.DiffProvider getDiffProvider() {
    return myDiffProvider;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Nullable
  public com.intellij.openapi.vcs.diff.RevisionSelector getRevisionSelector() {
    return myRevSelector;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"deprecation"})
  @Override
  @Nullable
  public VcsRevisionNumber parseRevisionNumber(String revision, FilePath path) {
    return parseRevisionNumber(revision);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"deprecation"})
  @Override
  @Nullable
  public VcsRevisionNumber parseRevisionNumber(String revision) {
    return parseRevisionNumber(revision, null);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isVersionedDirectory(VirtualFile dir) {
    return dir.isDirectory() && Util.rootOrNull(dir) != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void start() throws VcsException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void shutdown() throws VcsException {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void activate() {
    isActivated = true;
    myExecutableValidator = new ExecutableValidator(myProject);
    myExecutableValidator.checkExecutableAndShowDialogIfNeeded();
    if (!myProject.isDefault() && myRootTracker == null) {
      myRootTracker = new RootTracker(this, myProject, myRootListeners.getMulticaster());
    }
    if (myVfsListener == null) {
      myVfsListener = new VfsListener(myProject, this);
    }
    if (myConfigTracker == null) {
      myConfigTracker = new ConfigTracker(myProject, this, myConfigListeners.getMulticaster());
    }
    if (myIgnoreTracker == null) {
      myIgnoreTracker = new IgnoreTracker(myProject, this);
    }
    myIndexChangeListener.activate();
    myReferenceTracker.activate();
    NewUsersComponent.getInstance(myProject).activate();
    ProjectLogManager.getInstance(myProject).activate();
    //BranchConfigurations.getInstance(myProject).activate(); //TODO wc do we need this?
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void deactivate() {
    isActivated = false;
    BranchConfigurations.getInstance(myProject).deactivate();
    if (myRootTracker != null) {
      myRootTracker.dispose();
      myRootTracker = null;
    }
    if (myVfsListener != null) {
      Disposer.dispose(myVfsListener);
      myVfsListener = null;
    }
    if (myIgnoreTracker != null) {
      myIgnoreTracker.dispose();
      myIgnoreTracker = null;
    }
    if (myConfigTracker != null) {
      myConfigTracker.dispose();
      myConfigTracker = null;
    }
    myIndexChangeListener.dispose();
    myReferenceTracker.deactivate();
    NewUsersComponent.getInstance(myProject).deactivate();
    ProjectLogManager.getInstance(myProject).deactivate();
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  @Override
  public synchronized Configurable getConfigurable() {
    return myConfigurable;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public com.intellij.openapi.vcs.changes.ChangeProvider getChangeProvider() {
    return myChangeProvider;
  }

  /**
   * Show errors as popup and as messages in vcs view.
   *
   * @param list   a list of errors
   * @param action an action
   */
  public void showErrors(@NotNull List<VcsException> list, @NotNull String action) {
    if (list.size() > 0) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("\n");
      buffer.append(Bundle.message("error.list.title", action));
      for (final VcsException exception : list) {
        buffer.append("\n");
        buffer.append(exception.getMessage());
      }
      final String msg = buffer.toString();
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          Messages.showErrorDialog(myProject, msg, Bundle.getString("error.dialog.title"));
        }
      });
    }
  }

  /**
   * Show a plain message in vcs view
   *
   * @param message a message to show
   */
  public void showMessages(@NotNull String message) {
    if (message.length() == 0) return;
    showMessage(message, ConsoleViewContentType.NORMAL_OUTPUT.getAttributes());
  }

  /**
   * @return vcs settings for the current project
   */
  @NotNull
  public VcsSettings getSettings() {
    return mySettings;
  }

  /**
   * Show message in the VCS view
   *
   * @param message a message to show
   * @param style   a style to use
   */
  private void showMessage(@NotNull String message, final TextAttributes style) {
    myVcsManager.addMessageToConsoleWindow(message, style);
  }

  /**
   * Check version and report problem
   */
  public void checkVersion() {
    final String executable = mySettings.getPathToExecutable();
    synchronized (myCheckingVersion) {
      if (myVersion != null && myVersionCheckExcecutable.equals(executable)) {
        return;
      }
      myVersionCheckExcecutable = executable;
      // this assignment is done to prevent recursive version check
      myVersion = Version.INVALID;
      final String version;
      try {
        version = version(myProject).trim();
      }
      catch (VcsException e) {
        String reason = (e.getCause() != null ? e.getCause() : e).getMessage();
        if (!myProject.isDefault()) {
          showMessage(Bundle.message("vcs.unable.to.run", executable, reason), ConsoleViewContentType.SYSTEM_OUTPUT.getAttributes());
        }
        return;
      }
      myVersion = Version.parse(version);
      if (!Version.parse(version).isSupported() && !myProject.isDefault()) {
        showMessage(Bundle.message("vcs.unsupported.version", version, Version.MIN),
                    ConsoleViewContentType.SYSTEM_OUTPUT.getAttributes());
      }
    }
  }

  /**
   * @return the configured version of
   */
  public Version version() {
    checkVersion();
    return myVersion;
  }

  /**
   * Get the version of configured
   *
   * @param project the project
   * @return a version of configured
   * @throws com.intellij.openapi.vcs.VcsException an error if there is a problem with running
   */
  public static String version(Project project) throws VcsException {
    final String s;
    SimpleHandler h = new SimpleHandler(project, new File("."), Command.VERSION);
    h.setRemote(true);
    h.setSilent(true);
    s = h.run();
    return s;
  }

  /**
   * Show command line
   *
   * @param cmdLine a command line text
   */
  public void showCommandLine(final String cmdLine) {
    SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss.SSS");
    showMessage(f.format(new Date()) + ": " + cmdLine, ConsoleViewContentType.SYSTEM_OUTPUT.getAttributes());
  }

  /**
   * The error line
   *
   * @param line a line to show
   */
  public void showErrorMessages(final String line) {
    showMessage(line, ConsoleViewContentType.ERROR_OUTPUT.getAttributes());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean allowsNestedRoots() {
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <S> List<S> filterUniqueRoots(final List<S> in, final Convertor<S, VirtualFile> convertor) {
    Collections.sort(in, new ComparatorDelegate<S, VirtualFile>(convertor, FilePathComparator.getInstance()));

    for (int i = 1; i < in.size(); i++) {
      final S sChild = in.get(i);
      final VirtualFile child = convertor.convert(sChild);
      final VirtualFile childRoot = Util.rootOrNull(child);
      if (childRoot == null) {
        // non- file actually, skip it
        continue;
      }
      for (int j = i - 1; j >= 0; --j) {
        final S sParent = in.get(j);
        final VirtualFile parent = convertor.convert(sParent);
        // the method check both that parent is an ancestor of the child and that they share common  root
        if (VfsUtil.isAncestor(parent, child, false) && VfsUtil.isAncestor(childRoot, parent, false)) {
          in.remove(i);
          //noinspection AssignmentToForLoopParameter
          --i;
          break;
        }
      }
    }
    return in;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RootsConvertor getCustomConvertor() {
    return RootConverter.INSTANCE;
  }

  public static VcsKey getKey() {
    return ourKey;
  }

  @Override
  public VcsType getType() {
    return VcsType.centralized;
  }

  private final VcsOutgoingChangesProvider<CommittedChangeList> myOutgoingChangesProvider;

  @Override
  protected VcsOutgoingChangesProvider<CommittedChangeList> getOutgoingProviderImpl() {
    return myOutgoingChangesProvider;
  }

  @Override
  public RemoteDifferenceStrategy getRemoteDifferenceStrategy() {
    return RemoteDifferenceStrategy.ASK_TREE_PROVIDER;
  }

  @Override
  protected TreeDiffProvider getTreeDiffProviderImpl() {
    return myTreeDiffProvider;
  }

  @Override
  public List<CommitExecutor> getCommitExecutors() {
    return Collections.<CommitExecutor>singletonList(myCommitAndPushExecutor);
  }

  @Override
  public CommittedChangeList getRevisionChanges(final VcsFileRevision revision, final VirtualFile file) throws VcsException {
    final Project project = getProject();
    final VirtualFile vcsRoot = Util.getRoot(file);
    return ChangeUtils.getRevisionChanges(project, vcsRoot, revision.getRevisionNumber().asString(), false);
  }

  /**
   * @return true if vcs was activated
   */
  public boolean isActivated() {
    return isActivated;
  }

  public ExecutableValidator getExecutableValidator() {
    return myExecutableValidator;
  }

  public RepositoryChangeListener getIndexChangeListener() {
    return myIndexChangeListener;
  }

  /** {@inheritDoc} */
  @Override
  public com.intellij.openapi.vcs.EditFileProvider getEditFileProvider() {
    return myEditFileProvider;
  }
}
