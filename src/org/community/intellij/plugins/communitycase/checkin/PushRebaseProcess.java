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
package org.community.intellij.plugins.communitycase.checkin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Clock;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.DateFormatUtil;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.Handler;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.config.VcsSettings;
import org.community.intellij.plugins.communitycase.rebase.InteractiveRebaseEditorHandler;
import org.community.intellij.plugins.communitycase.rebase.RebaseEditorService;
import org.community.intellij.plugins.communitycase.update.BaseRebaseProcess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This is subclass of {@link org.community.intellij.plugins.communitycase.update.BaseRebaseProcess} that implement rebase operation for {@link PushActiveBranchesDialog}.
 * This operation reorders commits if needed.
 */
public class PushRebaseProcess extends BaseRebaseProcess {
  /**
   * The logger
   */
  private static final Logger LOG = Logger.getInstance("#"+PushRebaseProcess.class.getName());
  /**
   * Save changes policy
   */
  private final VcsSettings.UpdateChangesPolicy mySavePolicy;
  /**
   * The map from vcs root to list of the commit identifier for reordered commits, if vcs root is not provided, the reordering is not needed.
   */
  private final Map<VirtualFile, List<String>> myReorderedCommits;
  /**
   * A set of roots that have non-pushed merges
   */
  private final Set<VirtualFile> myRootsWithMerges;
  /**
   * The registration number for the rebase editor
   */
  private Integer myRebaseEditorNo;
  /**
   * The rebase editor service
   */
  private final RebaseEditorService myRebaseEditorService;

  /**
   * The constructor
   *
   * @param vcs             the vcs instance
   * @param project         the project instance
   * @param exceptions      the list of exceptions for the process
   * @param savePolicy      the save policy for the rebase process
   * @param rootsWithMerges a set of roots with merges
   */
  public PushRebaseProcess(final Vcs vcs,
                           final Project project,
                           List<VcsException> exceptions,
                           VcsSettings.UpdateChangesPolicy savePolicy,
                           Map<VirtualFile, List<String>> reorderedCommits,
                           Set<VirtualFile> rootsWithMerges) {
    super(vcs, project, exceptions);
    mySavePolicy = savePolicy;
    myReorderedCommits = reorderedCommits;
    myRootsWithMerges = rootsWithMerges;
    myRebaseEditorService = RebaseEditorService.getInstance();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected LineHandler makeStartHandler(VirtualFile root) throws VcsException {
    List<String> commits = myReorderedCommits.get(root);
    boolean hasMerges = myRootsWithMerges.contains(root);
    LineHandler h = new LineHandler(myProject, root, Command.REBASE);
    if (commits != null || hasMerges) {
      h.addParameters("-i");
      PushRebaseEditor pushRebaseEditor = new PushRebaseEditor(root, commits, hasMerges, h);
      myRebaseEditorNo = pushRebaseEditor.getHandlerNo();
      myRebaseEditorService.configureHandler(h, myRebaseEditorNo);
      if (hasMerges) {
        h.addParameters("-p");
      }
    }
    h.addParameters("-m", "-v");
    Branch currentBranch = Branch.current(myProject, root);
    assert currentBranch != null;
    Branch trackedBranch = currentBranch.tracked(myProject, root);
    assert trackedBranch != null;
    h.addParameters(trackedBranch.getFullName());
    return h;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void cleanupHandler(VirtualFile root, LineHandler h) {
    if (myRebaseEditorNo != null) {
      myRebaseEditorService.unregisterHandler(myRebaseEditorNo);
      myRebaseEditorNo = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureRebaseEditor(VirtualFile root, LineHandler h) {
    InteractiveRebaseEditorHandler editorHandler = new InteractiveRebaseEditorHandler(myRebaseEditorService, myProject, root, h);
    editorHandler.setRebaseEditorShown();
    myRebaseEditorNo = editorHandler.getHandlerNo();
    myRebaseEditorService.configureHandler(h, myRebaseEditorNo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String makeStashMessage() {
    return "Uncommitted changes before rebase operation in push dialog at " +
           DateFormatUtil.formatDateTime(Clock.getTime());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected VcsSettings.UpdateChangesPolicy getUpdatePolicy() {
    return mySavePolicy;
  }

  /**
   * The rebase editor that just overrides the list of commits
   */
  class PushRebaseEditor extends InteractiveRebaseEditorHandler {
    /**
     * The reordered commits
     */
    private final List<String> myCommits;
    /**
     * The true means that the root has merges
     */
    private final boolean myHasMerges;

    /**
     * The constructor from fields that is expected to be
     * accessed only from {@link org.community.intellij.plugins.communitycase.rebase.RebaseEditorService}.
     *
     * @param root      the git repository root
     * @param commits   the reordered commits
     * @param hasMerges if true, the vcs root has merges
     */
    public PushRebaseEditor(final VirtualFile root, List<String> commits, boolean hasMerges, Handler h) {
      super(myRebaseEditorService, myProject, root, h);
      myCommits = commits;
      myHasMerges = hasMerges;
    }

    /**
     * {@inheritDoc}
     */
    public int editCommits(String path) {
      if (!myRebaseEditorShown) {
        myRebaseEditorShown = true;
        if (myHasMerges) {
          return 0;
        }
        try {
          TreeMap<String, String> pickLines = new TreeMap<String, String>();
          StringScanner s = new StringScanner(new String(FileUtil.loadFileText(new File(path), Util.UTF8_ENCODING)));
          while (s.hasMoreData()) {
            if (!s.tryConsume("pick ")) {
              s.line();
              continue;
            }
            String commit = s.spaceToken();
            pickLines.put(commit, "pick " + commit + " " + s.line());
          }
          PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), Util.UTF8_ENCODING));
          try {
            for (String commit : myCommits) {
              String key = pickLines.headMap(commit + "\u0000").lastKey();
              if (key == null || !commit.startsWith(key)) {
                continue; // commit from merged branch
              }
              w.print(pickLines.get(key) + "\n");
            }
          }
          finally {
            w.close();
          }
          return 0;
        }
        catch (Exception ex) {
          LOG.error("Editor failed: ", ex);
          return 1;
        }
      }
      else {
        return super.editCommits(path);
      }
    }
  }
}
