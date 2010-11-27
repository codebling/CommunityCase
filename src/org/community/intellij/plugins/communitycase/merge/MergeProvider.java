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
package org.community.intellij.plugins.communitycase.merge;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.merge.MergeData;
import com.intellij.openapi.vcs.merge.MergeProvider2;
import com.intellij.openapi.vcs.merge.MergeSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.vcsUtil.VcsRunnable;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.FileRevision;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.FileUtils;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.RevisionNumber;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Merge-changes provider for Git, used by IDEA internal 3-way merge tool
 */
public class MergeProvider implements MergeProvider2 {
  /**
   * the logger
   */
  private static final Logger log = Logger.getInstance(MergeProvider.class.getName());
  /**
   * The project instance
   */
  private final Project myProject;
  /**
   * If true the merge provider has a reverse meaning
   */
  private final boolean myReverse;
  /**
   * The revision that designates common parent for the files during the merge
   */
  private static final int ORIGINAL_REVISION_NUM = 1;
  /**
   * The revision that designates the file on the local branch
   */
  private static final int YOURS_REVISION_NUM = 2;
  /**
   * The revision that designates the remote file being merged
   */
  private static final int THEIRS_REVISION_NUM = 3;

  /**
   * A merge provider
   *
   * @param project a project for the provider
   */
  public MergeProvider(Project project) {
    this(project, false);
  }

  /**
   * A merge provider
   *
   * @param project a project for the provider
   * @param reverse if true, yours and theirs take a reverse meaning
   */
  public MergeProvider(Project project, boolean reverse) {
    myProject = project;
    myReverse = reverse;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public MergeData loadRevisions(final VirtualFile file) throws VcsException {
    final MergeData mergeData = new MergeData();
    if (file == null) return mergeData;
    final VirtualFile root = Util.getRoot(file);
    final FilePath path = VcsUtil.getFilePath(file.getPath());

    VcsRunnable runnable = new VcsRunnable() {
      @SuppressWarnings({"ConstantConditions"})
      public void run() throws VcsException {
        FileRevision original = new FileRevision(myProject, path, new RevisionNumber(":" + ORIGINAL_REVISION_NUM));
        FileRevision current = new FileRevision(myProject, path, new RevisionNumber(":" + yoursRevision()));
        FileRevision last = new FileRevision(myProject, path, new RevisionNumber(":" + theirsRevision()));
        try {
          try {
            mergeData.ORIGINAL = original.getContent();
          }
          catch (Exception ex) {
            /// unable to load original revision, use the current instead
            /// This could happen in case if rebasing.
            mergeData.ORIGINAL = file.contentsToByteArray();
          }
          mergeData.CURRENT = current.getContent();
          mergeData.LAST = last.getContent();
          try {
            mergeData.LAST_REVISION_NUMBER = RevisionNumber.resolve(myProject, root, myReverse ? "HEAD" : "MERGE_HEAD");
          }
          catch (VcsException e) {
            // ignore exception, the null value will be used
          }
        }
        catch (IOException e) {
          throw new IllegalStateException("Failed to load file content", e);
        }
      }
    };
    VcsUtil.runVcsProcessWithProgress(runnable, Bundle.message("merge.load.files"), false, myProject);
    return mergeData;
  }

  /**
   * @return number for "yours" revision  (taking {@code revsere} flag in account)
   */
  private int yoursRevision() {
    return myReverse ? THEIRS_REVISION_NUM : YOURS_REVISION_NUM;
  }

  /**
   * @return number for "theirs" revision (taking {@code revsere} flag in account)
   */
  private int theirsRevision() {
    return myReverse ? YOURS_REVISION_NUM : THEIRS_REVISION_NUM;
  }

  /**
   * {@inheritDoc}
   */
  public void conflictResolvedForFile(VirtualFile file) {
    if (file == null) return;
    try {
      FileUtils.addFiles(myProject, Util.getRoot(file), file);
    }
    catch (VcsException e) {
      log.error("Confirming conflict resolution failed", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean isBinary(VirtualFile file) {
    return file.getFileType().isBinary();
  }

  @NotNull
  public MergeSession createMergeSession(List<VirtualFile> files) {
    return new MyMergeSession(files);
  }


  /**
   * The conflict descriptor
   */
  private static class Conflict {
    /**
     * the file in the conflict
     */
    VirtualFile myFile;
    /**
     * the root for the file
     */
    VirtualFile myRoot;
    /**
     * the status of theirs revision
     */
    Status myStatusTheirs;
    /**
     * the status
     */
    Status myStatusYours;

    /**
     * @return true if the merge operation can be applied
     */
    boolean isMergeable() {
      return myStatusTheirs == Conflict.Status.MODIFIED && myStatusYours == Conflict.Status.MODIFIED;
    }

    /**
     * The conflict status
     */
    enum Status {
      /**
       * the file was modified on the branch
       */
      MODIFIED,
      /**
       * the file was deleted on the branch
       */
      DELETED,
    }
  }


  /**
   * The merge session, it queries conflict information .
   */
  private class MyMergeSession implements MergeSession {
    /**
     * the map with conflicts
     */
    Map<VirtualFile, Conflict> myConflicts = new HashMap<VirtualFile, Conflict>();

    /**
     * A constructor from list of the files
     *
     * @param filesToMerge the files to process using merge dialog.
     */
    MyMergeSession(List<VirtualFile> filesToMerge) {
      // get conflict type by the file
      try {
        for (Map.Entry<VirtualFile, List<VirtualFile>> e : Util.sortFilesByRoot(filesToMerge).entrySet()) {
          Map<String, Conflict> cs = new HashMap<String, Conflict>();
          VirtualFile root = e.getKey();
          List<VirtualFile> files = e.getValue();
          SimpleHandler h = new SimpleHandler(myProject, root, Command.LS_FILES);
          h.setNoSSH(true);
          h.setStdoutSuppressed(true);
          h.addParameters("--exclude-standard", "--unmerged", "-t", "-z");
          h.endOptions();
          String output = h.run();
          StringScanner s = new StringScanner(output);
          while (s.hasMoreData()) {
            if (!"M".equals(s.spaceToken())) {
              s.boundedToken('\u0000');
              continue;
            }
            s.spaceToken(); // permissions
            s.spaceToken(); // commit hash
            int source = Integer.parseInt(s.tabToken());
            String file = s.boundedToken('\u0000');
            Conflict c = cs.get(file);
            if (c == null) {
              c = new Conflict();
              c.myRoot = root;
              cs.put(file, c);
            }
            if (source == theirsRevision()) {
              c.myStatusTheirs = Conflict.Status.MODIFIED;
            }
            else if (source == yoursRevision()) {
              c.myStatusYours = Conflict.Status.MODIFIED;
            }
            else if (source != ORIGINAL_REVISION_NUM) {
              throw new IllegalStateException("Unknown revision " + source + " for the file: " + file);
            }
          }
          for (VirtualFile f : files) {
            String path = Util.relativePath(root, f);
            Conflict c = cs.get(path);
            assert c != null : "The conflict not found for the file: " + f.getPath() + "(" + path + ")";
            c.myFile = f;
            if (c.myStatusTheirs == null) {
              c.myStatusTheirs = Conflict.Status.DELETED;
            }
            if (c.myStatusYours == null) {
              c.myStatusYours = Conflict.Status.DELETED;
            }
            myConflicts.put(f, c);
          }
        }
      }
      catch (VcsException ex) {
        throw new IllegalStateException("The git operation should not fail in this context", ex);
      }
    }

    /**
     * {@inheritDoc}
     */
    public ColumnInfo[] getMergeInfoColumns() {
      return new ColumnInfo[]{new StatusColumn(false), new StatusColumn(true)};
    }

    /**
     * {@inheritDoc}
     */
    public boolean canMerge(VirtualFile file) {
      Conflict c = myConflicts.get(file);
      return c != null && c.isMergeable();
    }

    /**
     * {@inheritDoc}
     */
    public void conflictResolvedForFile(VirtualFile file, Resolution resolution) {
      Conflict c = myConflicts.get(file);
      assert c != null : "Conflict was not loaded for the file: " + file.getPath();
      try {
        if (c.isMergeable()) {
          FileUtils.addFiles(myProject, c.myRoot, file);
        }
        else {
          Conflict.Status status;
          switch (resolution) {
            case AcceptedTheirs:
              status = c.myStatusTheirs;
              break;
            case AcceptedYours:
              status = c.myStatusYours;
              break;
            case Merged:
            default:
              throw new IllegalArgumentException("Unsupported resolution for unmergable files(" + file.getPath() + "): " + resolution);
          }
          switch (status) {
            case MODIFIED:
              FileUtils.addFiles(myProject, c.myRoot, file);
              break;
            case DELETED:
              FileUtils.deleteFiles(myProject, c.myRoot, file);
              break;
            default:
              throw new IllegalArgumentException("Unsupported status(" + file.getPath() + "): " + status);
          }
        }
      }
      catch (VcsException e) {
        log.error("Unexpected exception during the git operation (" + file.getPath() + ")", e);
      }
    }

    /**
     * The status column, the column shows either "yours" or "theirs" status
     */
    class StatusColumn extends ColumnInfo<VirtualFile, String> {
      /**
       * if false, "yours" status is displayed, otherwise "theirs"
       */
      private final boolean myIsTheirs;

      /**
       * The constructor
       *
       * @param isTheirs if true columns represents status in 'theirs' revision, if false in 'ours'
       */
      public StatusColumn(boolean isTheirs) {
        super(isTheirs ? Bundle.message("merge.tool.column.theirs.status") : Bundle.message("merge.tool.column.yours.status"));
        myIsTheirs = isTheirs;
      }

      /**
       * {@inheritDoc}
       */
      public String valueOf(VirtualFile file) {
        Conflict c = myConflicts.get(file);
        assert c != null : "No conflict for the file " + file;
        Conflict.Status s = myIsTheirs ? c.myStatusTheirs : c.myStatusYours;
        switch (s) {
          case MODIFIED:
            return Bundle.message("merge.tool.column.status.modified");
          case DELETED:
            return Bundle.message("merge.tool.column.status.deleted");
          default:
            throw new IllegalStateException("Unknown status " + s + " for file " + file.getPath());
        }
      }
    }
  }
}
