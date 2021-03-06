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
package org.community.intellij.plugins.communitycase.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.ContentRevision;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * repository change provider
 */
public class ChangeProvider implements com.intellij.openapi.vcs.changes.ChangeProvider {
  private static final Logger LOG = Logger.getInstance("#"+ChangeProvider.class.getName());
  private final Project myProject;
  private final ChangeListManager myChangeListManager;
  private FileDocumentManager myFileDocumentManager;
  private final ProjectLevelVcsManager myVcsManager;

  public ChangeProvider(@NotNull Project project, ChangeListManager changeListManager, FileDocumentManager fileDocumentManager, ProjectLevelVcsManager vcsManager) {
    myProject = project;
    myChangeListManager = changeListManager;
    myFileDocumentManager = fileDocumentManager;
    myVcsManager = vcsManager;
  }

  /** {@inheritDoc} */
  @Override
  public void getChanges(final VcsDirtyScope dirtyScope,
                         final ChangelistBuilder builder,
                         final ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    progress.start();

    final Collection<VirtualFile> affected = dirtyScope.getAffectedContentRootsWithCheck();
    if (dirtyScope.getAffectedContentRoots().size() != affected.size()) {
      final Set<VirtualFile> set = new HashSet<VirtualFile>(affected);
      set.removeAll(dirtyScope.getAffectedContentRoots());
      for (VirtualFile file : set) {
        ((VcsModifiableDirtyScope) dirtyScope).addDirtyDirRecursively(new FilePathImpl(file));
      }
    }
    Collection<VirtualFile> roots = Util.rootsForPaths(affected);

    try {
      final MyNonChangedHolder holder = new MyNonChangedHolder(myProject, dirtyScope.getDirtyFilesNoExpand(), addGate,
                                                               myFileDocumentManager, myVcsManager);
      for (VirtualFile root : roots) {
        ChangeCollector c = new ChangeCollector(myProject, myChangeListManager, progress, dirtyScope, root);
        final Collection<Change> changes = c.changes();
        holder.changed(changes);
        for (Change file : changes) {
          builder.processChange(file, Vcs.getKey());
        }
        for (VirtualFile f : c.unversioned()) {
          builder.processUnversionedFile(f);
          holder.unversioned(f);
        }
        holder.feedBuilder(builder);
      }
    } catch (VcsException e) {// most probably the error happened because is not configured
      final Vcs vcs = Vcs.getInstance(myProject);
      if (vcs != null) {
        vcs.getExecutableValidator().showNotificationOrThrow(e);
      }
    }
    progress.stop();
  }

  private static class MyNonChangedHolder {
    private final Project myProject;
    private final Set<FilePath> myDirty;
    private final ChangeListManagerGate myAddGate;
    private FileDocumentManager myFileDocumentManager;
    private ProjectLevelVcsManager myVcsManager;

    private MyNonChangedHolder(final Project project,
                               final Set<FilePath> dirty,
                               final ChangeListManagerGate addGate,
                               FileDocumentManager fileDocumentManager, ProjectLevelVcsManager vcsManager) {
      myProject = project;
      myDirty = dirty;
      myAddGate = addGate;
      myFileDocumentManager = fileDocumentManager;
      myVcsManager = vcsManager;
    }

    public void changed(final Collection<Change> changes) {
      for (Change change : changes) {
        final FilePath beforePath = ChangesUtil.getBeforePath(change);
        if (beforePath != null) {
          myDirty.remove(beforePath);
        }
        final FilePath afterPath = ChangesUtil.getAfterPath(change);
        if (afterPath != null) {
          myDirty.remove(afterPath);
        }
      }
    }

    public void unversioned(final VirtualFile vf) {
      myDirty.remove(new FilePathImpl(vf));
    }

    public void feedBuilder(final ChangelistBuilder builder) throws VcsException {
      final VcsKey Key = Vcs.getKey();

      for (FilePath filePath : myDirty) {
        final VirtualFile vf = filePath.getVirtualFile();
        if (vf != null
                && !FileStatus.ADDED.equals(myAddGate.getStatus(vf))
                && myFileDocumentManager.isFileModified(vf)
//                && myFileDocumentManager.isDocumentUnsaved(myFileDocumentManager.getDocument(vf))
                ) {
          final VirtualFile root = myVcsManager.getVcsRootFor(vf);
          if (root != null) {
            //todo wc fix this
            //final VcsRevisionNumber beforeRevisionNumber = ChangeUtils.loadRevision(myProject, root, "HEAD");
//              builder.processChange(new Change(ContentRevision.createRevision(vf, beforeRevisionNumber, myProject),
            builder.processChange(new Change(ContentRevision.createRevision(vf, null, myProject),
                    ContentRevision.createRevision(vf, null, myProject), FileStatus.MODIFIED), Key);
          }
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isModifiedDocumentTrackingRequired() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void doCleanup(final List<VirtualFile> files) {
  }
}
