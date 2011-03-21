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
package org.community.intellij.plugins.communitycase.diff;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import gnu.trove.THashSet;
import org.community.intellij.plugins.communitycase.FileRevision;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.history.HistoryUtils;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * diff provider
 */
public class DiffProvider implements com.intellij.openapi.vcs.diff.DiffProvider {
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * The status manager for the project
   */
  private final FileStatusManager myStatusManager;
  /**
   *
   */
  private static final Set<FileStatus> ourGoodStatuses;

  static {
    ourGoodStatuses = new THashSet<FileStatus>();
    ourGoodStatuses.addAll(
      Arrays.asList(FileStatus.NOT_CHANGED, FileStatus.DELETED, FileStatus.MODIFIED, FileStatus.MERGE, FileStatus.MERGED_WITH_CONFLICTS));
  }

  /**
   * A constructor
   *
   * @param project the context project
   */
  public DiffProvider(@NotNull Project project) {
    myProject = project;
    myStatusManager = FileStatusManager.getInstance(myProject);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public VcsRevisionNumber getCurrentRevision(VirtualFile file) {
    if (file.isDirectory()) {
      return null;
    }
    try {
      return HistoryUtils.getCurrentRevision(myProject,file);
    }
    catch (VcsException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public ItemLatestState getLastRevision(VirtualFile file) {
    if (file.isDirectory()) {
      return null;
    }
    if (!ourGoodStatuses.contains(myStatusManager.getStatus(file))) {
      return null;
    }
    try {
      return HistoryUtils.getLastRevision(myProject, VcsUtil.getFilePath(file.getPath()));
    }
    catch (VcsException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public ContentRevision createFileContent(VcsRevisionNumber revisionNumber, VirtualFile selectedFile) {
    if (selectedFile.isDirectory()) {
      return null;
    }
    final String path = selectedFile.getPath();
    if (Util.rootOrNull(selectedFile) == null) {
      return null;
    }
    try {
      FilePath filePath = VcsUtil.getFilePath(path);
      for (VcsFileRevision f : HistoryUtils.history(myProject, filePath)) {
        FileRevision Revision = (FileRevision)f;
        if (f.getRevisionNumber().equals(revisionNumber)) {
          return new org.community.intellij.plugins.communitycase.ContentRevision(Revision.getPath(), (VcsRevisionNumber)revisionNumber, myProject, selectedFile.getCharset());
        }
      }
      ContentRevision candidate =
        new org.community.intellij.plugins.communitycase.ContentRevision(filePath, (VcsRevisionNumber)revisionNumber, myProject, selectedFile.getCharset());
      try {
        candidate.getContent();
        return candidate;
      }
      catch (VcsException e) {
        // file does not exists
      }
    }
    catch (VcsException e) {
      Vcs.getInstance(myProject).showErrors(Collections.singletonList(e), Bundle.message("diff.find.error", path));
    }
    return null;
  }

  public ItemLatestState getLastRevision(FilePath filePath) {
    if (filePath.isDirectory()) {
      return null;
    }
    final VirtualFile vf = filePath.getVirtualFile();
    if (vf != null) {
      if (! ourGoodStatuses.contains(myStatusManager.getStatus(vf))) {
        return null;
      }
    }
    try {
      return HistoryUtils.getLastRevision(myProject, filePath);
    }
    catch (VcsException e) {
      return null;
    }
  }

  public VcsRevisionNumber getLatestCommittedRevision(VirtualFile vcsRoot) {
    // todo
    return null;
  }
}
