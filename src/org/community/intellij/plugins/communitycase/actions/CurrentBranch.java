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
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.ui.CurrentBranchDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * action for showing the current branch
 */
public class CurrentBranch extends RepositoryAction {

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  protected String getActionName() {
    return Bundle.getString("current.branch.action.name");
  }

  /**
   * {@inheritDoc}
   */
  protected void perform(@NotNull final Project project,
                         @NotNull final List<VirtualFile> Roots,
                         @NotNull final VirtualFile defaultRoot,
                         final Set<VirtualFile> affectedRoots,
                         final List<VcsException> exceptions) throws VcsException {
    CurrentBranchDialog dialog = new CurrentBranchDialog(project, Roots, defaultRoot);
    dialog.show();
    if (dialog.isOK()) {
      dialog.updateTrackedBranch();
    }
  }
}
