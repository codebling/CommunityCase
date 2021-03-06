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
package org.community.intellij.plugins.communitycase.vfs;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsVFSListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.util.ui.UIUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.FileUtils;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * virtual file adapter
 */
public class VfsListener extends VcsVFSListener {
  /**
   * More than zero if events are suppressed
   */
  final AtomicInteger myEventsSuppressLevel = new AtomicInteger(0);

  /**
   * A constructor for listener
   *
   * @param project a project
   * @param vcs     a vcs for that project
   */
  public VfsListener(final Project project, final Vcs vcs) {
    super(project, vcs);
  }

  /**
   * Set events suppressed, the events should be unsuppressed later
   *
   * @param value true if events should be suppressed, false otherwise
   */
  public void setEventsSuppressed(boolean value) {
    if (value) {
      myEventsSuppressLevel.incrementAndGet();
    }
    else {
      int v = myEventsSuppressLevel.decrementAndGet();
      assert v >= 0;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean isEventIgnored(VirtualFileEvent event, boolean putInDirty) {
    return super.isEventIgnored(event, putInDirty) || myEventsSuppressLevel.get() != 0;
  }

  /**
   * {@inheritDoc}
   */
  protected String getAddTitle() {
    return Bundle.getString("vfs.listener.add.title");
  }

  /**
   * {@inheritDoc}
   */
  protected String getSingleFileAddTitle() {
    return Bundle.getString("vfs.listener.add.single.title");
  }

  /**
   * {@inheritDoc}
   */
  protected String getSingleFileAddPromptTemplate() {
    return Bundle.getString("vfs.listener.add.single.prompt");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void executeAdd(final List<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copiedFiles) {
    // Filter added files before further processing
    final Map<VirtualFile, List<VirtualFile>> sortedFiles;
    try {
      sortedFiles = Util.sortFilesByRoot(addedFiles, true);
    }
    catch (VcsException e) {
      throw new RuntimeException("The exception is not expected here", e);
    }
    final HashSet<VirtualFile> retainedFiles = new HashSet<VirtualFile>();
    final ProgressManager progressManager = ProgressManager.getInstance();
    progressManager.run(new Task.Backgroundable(myProject, Bundle.getString("vfs.listener.checking.ignored"), false) {
      @Override
      public void run(@NotNull ProgressIndicator pi) {
        for (Map.Entry<VirtualFile, List<VirtualFile>> e : sortedFiles.entrySet()) {
          VirtualFile root = e.getKey();
          pi.setText(root.getPresentableUrl());
          for (List<String> paths : FileUtils.chunkFiles(root, e.getValue())) {
            pi.setText2(paths.get(0) + "...");
            try {
              SimpleHandler h = new SimpleHandler(myProject, root, Command.LS_FILES);
              h.setRemote(true);
              h.addParameters("--exclude-standard", "--others");
              h.endOptions();
              h.addParameters(paths);
              for (StringScanner s = new StringScanner(h.run()); s.hasMoreData();) {
                String l = s.line();
                String p = Util.unescapePath(l);
                VirtualFile f = root.findFileByRelativePath(p);
                assert f != null : "The virtual file must be available at this point: " + p + " (" + root.getPresentableUrl() + ")";
                retainedFiles.add(f);
              }
            }
            catch (final VcsException ex) {
              UIUtil.invokeLaterIfNeeded(new Runnable() {
                public void run() {
                  Vcs().showMessages(ex.getMessage());
                }
              });
            }
          }
          addedFiles.retainAll(retainedFiles);
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              originalExecuteAdd(addedFiles, copiedFiles);
            }
          });
        }
      }
    });
  }

  /**
   * The version of execute add before overriding
   *
   * @param addedFiles  the added files
   * @param copiedFiles the copied files
   */
  private void originalExecuteAdd(List<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copiedFiles) {
    super.executeAdd(addedFiles, copiedFiles);
  }

  /**
   * {@inheritDoc}
   */
  protected void performAdding(final Collection<VirtualFile> addedFiles, final Map<VirtualFile, VirtualFile> copyFromMap) {
    final Map<VirtualFile, List<VirtualFile>> sortedFiles;
    try {
      sortedFiles = Util.sortFilesByRoot(addedFiles, true);
    }
    catch (VcsException e) {
      Vcs().showMessages(e.getMessage());
      return;
    }
    Vcs().runInBackground(new Task.Backgroundable(myProject, Bundle.getString("add.adding")) {

      public void run(@NotNull ProgressIndicator indicator) {
        // note that copied files are not processed because they are included into added files.
        for (Map.Entry<VirtualFile, List<VirtualFile>> e : sortedFiles.entrySet()) {
          try {
            final VirtualFile root = e.getKey();
            indicator.setText(root.getPresentableUrl());
            FileUtils.addFiles(myProject, root, e.getValue());
            Util.markFilesDirty(myProject, e.getValue());
          }
          catch (final VcsException ex) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              public void run() {
                Vcs().showMessages(ex.getMessage());
              }
            });
          }
        }
      }
    });
  }

  /**
   * @return casted vcs instance
   */
  private Vcs Vcs() {
    return ((Vcs)myVcs);
  }

  /**
   * Perform adding the files using file paths
   *
   * @param addedFiles the added files
   */
  private void performAdding(Collection<FilePath> addedFiles) {
    final Map<VirtualFile, List<FilePath>> sortedFiles;
    try {
      sortedFiles = Util.sortFilePathsByRoot(addedFiles, true);
    }
    catch (VcsException e) {
      Vcs().showMessages(e.getMessage());
      return;
    }
    Vcs().runInBackground(new Task.Backgroundable(myProject, Bundle.getString("add.adding")) {

      public void run(@NotNull ProgressIndicator indicator) {
        for (Map.Entry<VirtualFile, List<FilePath>> e : sortedFiles.entrySet()) {
          try {
            final VirtualFile root = e.getKey();
            indicator.setText(root.getPresentableUrl());
            FileUtils.addPaths(myProject, root, e.getValue());
            Util.markFilesDirty(myProject, e.getValue());
          }
          catch (final VcsException ex) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              public void run() {
                Vcs().showMessages(ex.getMessage());
              }
            });
          }
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  protected String getDeleteTitle() {
    return Bundle.getString("vfs.listener.delete.title");
  }

  /**
   * {@inheritDoc}
   */
  protected String getSingleFileDeleteTitle() {
    return Bundle.getString("vfs.listener.delete.single.title");
  }

  /**
   * {@inheritDoc}
   */
  protected String getSingleFileDeletePromptTemplate() {
    return Bundle.getString("vfs.listener.delete.single.prompt");
  }

  /**
   * {@inheritDoc}
   */
  protected void performDeletion(final List<FilePath> filesToDelete) {
    final Map<VirtualFile, List<FilePath>> sortedFiles;
    try {
      sortedFiles = Util.sortFilePathsByRoot(filesToDelete, true);
    }
    catch (VcsException e) {
      Vcs().showMessages(e.getMessage());
      return;
    }
    Vcs().runInBackground(new Task.Backgroundable(myProject, Bundle.getString("remove.removing")) {
      public void run(@NotNull ProgressIndicator indicator) {
        HashSet<File> filesToRefresh = new HashSet<File>();
        for (Map.Entry<VirtualFile, List<FilePath>> e : sortedFiles.entrySet()) {
          try {
            final VirtualFile root = e.getKey();
            final File rootFile = new File(root.getPath());
            indicator.setText(root.getPresentableUrl());
            FileUtils.delete(myProject, root, e.getValue(), "--ignore-unmatch");
            if (myProject != null && !myProject.isDisposed()) {
              Util.markFilesDirty(myProject, e.getValue());
            }
            for (FilePath p : e.getValue()) {
              for (File f = p.getIOFile(); f != null && !f.equals(rootFile); f = f.getParentFile()) {
                filesToRefresh.add(f);
              }
            }
          }
          catch (final VcsException ex) {
            UIUtil.invokeLaterIfNeeded(new Runnable() {
              public void run() {
                Vcs().showMessages(ex.getMessage());
              }
            });
          }
        }
        LocalFileSystem.getInstance().refreshIoFiles(filesToRefresh);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  protected void performMoveRename(final List<MovedFileInfo> movedFiles) {
    // because does not tracks moves, the file are just added and deleted.
    ArrayList<FilePath> added = new ArrayList<FilePath>();
    ArrayList<FilePath> removed = new ArrayList<FilePath>();
    for (MovedFileInfo m : movedFiles) {
      added.add(VcsUtil.getFilePath(m.myNewPath));
      removed.add(VcsUtil.getFilePath(m.myOldPath));
    }
    performAdding(added);
    performDeletion(removed);
  }

  /**
   * {@inheritDoc}
   */
  protected boolean isDirectoryVersioningSupported() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected Collection<FilePath> selectFilePathsToDelete(final List<FilePath> deletedFiles) {
    // For asking about vcs delete does not make much sense. The result is practically identical.
    return deletedFiles;
  }
}
