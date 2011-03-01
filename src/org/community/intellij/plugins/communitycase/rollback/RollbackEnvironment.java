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
package org.community.intellij.plugins.communitycase.rollback;

import com.intellij.lifecycle.PeriodicalTasksCloser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.rollback.RollbackProgressListener;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.FileUtils;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.config.VcsSettings;
import org.community.intellij.plugins.communitycase.edit.EditFileProvider;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * rollback/revert environment
 */
public class RollbackEnvironment implements com.intellij.openapi.vcs.rollback.RollbackEnvironment {
  /**
   * The project
   */
  private final Project myProject;

  /**
   * A constructor
   *
   * @param project the context project
   */
  public RollbackEnvironment(@NotNull Project project) {
    myProject = project;
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getRollbackOperationName() {
    return Bundle.getString("revert.action.name");
  }

  /**
   * {@inheritDoc}
   */
  public void rollbackModifiedWithoutCheckout(@NotNull List<VirtualFile> files,
                                              final List<VcsException> exceptions,
                                              final RollbackProgressListener listener) {
    throw new UnsupportedOperationException("Explicit file checkout is not supported by .");
  }

  /**
   * {@inheritDoc}
   */
  public void rollbackMissingFileDeletion(@NotNull List<FilePath> files,
                                          final List<VcsException> exceptions,
                                          final RollbackProgressListener listener) {
    throw new UnsupportedOperationException("Missing file delete is not reported by .");
  }

  /**
   * {@inheritDoc}
   */
  public void rollbackIfUnchanged(@NotNull VirtualFile file) {
    // do nothing
  }

  /**
   * {@inheritDoc}
   */
  public void rollbackChanges(@NotNull List<Change> changes,
                              final List<VcsException> exceptions,
                              @NotNull final RollbackProgressListener listener) {
    HashMap<VirtualFile, List<FilePath>> toUndoHijack = new HashMap<VirtualFile, List<FilePath>>();
    HashMap<VirtualFile, List<FilePath>> toUndoCheckout = new HashMap<VirtualFile, List<FilePath>>();
    List<FilePath> toDelete = new ArrayList<FilePath>();

    listener.determinate();
    // collect changes to revert
    for (Change c : changes) {
      switch (c.getType()) {
        //todo wc handle new, moved, deleted
        case NEW:
          //undo dir checkout
          //rmname file
          //delete file
          break;
        case MOVED:
          //DON'T undo file checkout
          //move file back
          //undo dir checkout
          registerFile(toUndoCheckout, c.getBeforeRevision().getFile(), exceptions);
          toDelete.add(c.getAfterRevision().getFile());
          break;
        case MODIFICATION:
          //undo file checkout
          if(c.getFileStatus()== FileStatus.HIJACKED)
            registerFile(toUndoHijack,c.getBeforeRevision().getFile(),exceptions);
          else
            registerFile(toUndoCheckout,c.getBeforeRevision().getFile(),exceptions);
          break;
        case DELETED:
          //undo dir checkout
          //undo file checkout
          //update file
          registerFile(toUndoCheckout, c.getBeforeRevision().getFile(), exceptions);
          break;
      }
    }
    /*
    // delete files
    for (FilePath file : toDelete) {
      listener.accept(file);
      try {
        final File ioFile = file.getIOFile();
        if (ioFile.exists()) {
          if (!ioFile.delete()) {
            //noinspection ThrowableInstanceNeverThrown
            exceptions.add(new VcsException("Unable to delete file: " + file));
          }
        }
      }
      catch (Exception e) {
        //noinspection ThrowableInstanceNeverThrown
        exceptions.add(new VcsException("Unable to delete file: " + file, e));
      }
    }
    */
    // revert files from HEAD
    for (Map.Entry<VirtualFile, List<FilePath>> entry : toUndoCheckout.entrySet()) {
      listener.accept(entry.getValue());
      try {
        undoCheckout(entry.getKey(), entry.getValue());
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }
    // revert files from HEAD
    for (Map.Entry<VirtualFile, List<FilePath>> entry : toUndoHijack.entrySet()) {
      listener.accept(entry.getValue());
      try {
        undoHijack(entry.getKey(), entry.getValue());
      }
      catch (VcsException e) {
        exceptions.add(e);
      }
    }
    LocalFileSystem lfs = LocalFileSystem.getInstance();
    HashSet<File> filesToRefresh = new HashSet<File>();
    for (Change c : changes) {
      ContentRevision before = c.getBeforeRevision();
      if (before != null) {
        filesToRefresh.add(new File(before.getFile().getPath()));
      }
      ContentRevision after = c.getAfterRevision();
      if (after != null) {
        filesToRefresh.add(new File(after.getFile().getPath()));
      }
    }
    lfs.refreshIoFiles(filesToRefresh);
  }

  /**
   * Reverts the list of files we are passed.
   *
   * @param root  the VCS root
   * @param files The array of files for which to undo checkout.
   * @throws VcsException Id it breaks.
   */
  private void undoHijack(VirtualFile root, List<FilePath> files) throws VcsException {
    for (List<String> paths : FileUtils.chunkPaths(root, files)) {
      SimpleHandler handler = new SimpleHandler(myProject, root, Command.CHECKOUT);
      handler.setRemote(true);
      handler.addParameters("-unr");//unreserved
      handler.addParameters("-nc");//no comment
      handler.addParameters("-use"); //use hijacked file for checkout
      handler.endOptions();
      handler.addParameters(paths);
      handler.run();
    }

    for (List<String> paths : FileUtils.chunkPaths(root, files)) {
      SimpleHandler handler = new SimpleHandler(myProject, root, Command.UNDO_CHECKOUT);
      handler.setRemote(true);
      VcsSettings settings=VcsSettings.getInstance(myProject);
      if(settings!=null && !settings.isPreserveKeepFiles())
        handler.addParameters("-rm");
      else
        handler.addParameters("-kee");
      handler.endOptions();
      handler.addParameters(paths);
      handler.run();
    }
  }

  /**
   * Reverts the list of files we are passed.
   *
   * @param root  the VCS root
   * @param files The array of files for which to undo checkout.
   * @throws VcsException Id it breaks.
   */
  public void undoCheckout(final VirtualFile root, final List<FilePath> files) throws VcsException {
    for (List<String> paths : FileUtils.chunkPaths(root, files)) {
      SimpleHandler handler = new SimpleHandler(myProject, root, Command.UNDO_CHECKOUT);
      handler.setRemote(true);
      VcsSettings settings=VcsSettings.getInstance(myProject);
      if(settings!=null && !settings.isPreserveKeepFiles())
        handler.addParameters("-rm");
      else
        handler.addParameters("-kee");
      handler.endOptions();
      handler.addParameters(paths);
      handler.run();
    }
  }

  /**
   * Register file in the map under appropriate root
   *
   * @param files      a map to use
   * @param file       a file to register
   * @param exceptions the list of exceptions to update
   */
  private static void registerFile(Map<VirtualFile, List<FilePath>> files, FilePath file, List<VcsException> exceptions) {
    final VirtualFile root;
    try {
      root = Util.getRoot(file);
    }
    catch (VcsException e) {
      exceptions.add(e);
      return;
    }
    List<FilePath> paths = files.get(root);
    if (paths == null) {
      paths = new ArrayList<FilePath>();
      files.put(root, paths);
    }
    paths.add(file);
  }

  /**
   * Get instance of the service
   *
   * @param project a context project
   * @return a project-specific instance of the service
   */
  public static RollbackEnvironment getInstance(final Project project) {
    return PeriodicalTasksCloser.getInstance().safeGetService(project, RollbackEnvironment.class);
  }
}
