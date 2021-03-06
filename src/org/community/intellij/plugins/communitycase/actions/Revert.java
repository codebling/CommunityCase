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
package org.community.intellij.plugins.communitycase.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ui.RollbackChangesDialog;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * "revert" action
 */
public class Revert extends BasicAction {

  private static final String NAME = Bundle.getString("revert.action.name");

  public Revert() {
    super(NAME);
  }

  @Override
  public boolean perform(@NotNull final Project project, Vcs vcs, @NotNull final List<VcsException> exceptions, @NotNull VirtualFile[] affectedFiles) {
    final List<Change> changes = new ArrayList<Change>(affectedFiles.length);
    for (VirtualFile f : affectedFiles) {
      Change ch = ChangeListManager.getInstance(project).getChange(f);
      if (ch != null) {
        changes.add(ch);
      }
    }
    RollbackChangesDialog.rollbackChanges(project, changes);
    return false;
  }

  @Override
  @NotNull
  protected String getActionName() {
    return NAME;
  }

  @Override
  protected boolean isEnabled(@NotNull Project project, @NotNull Vcs vcs, @NotNull VirtualFile... vFiles) {
    for (VirtualFile file : vFiles) {
      FileStatus status = FileStatusManager.getInstance(project).getStatus(file);
      if (status == FileStatus.UNKNOWN || status == FileStatus.NOT_CHANGED) return false;
    }
    return true;
  }
}
