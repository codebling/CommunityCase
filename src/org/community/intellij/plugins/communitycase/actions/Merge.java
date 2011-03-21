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

import com.intellij.history.Label;
import com.intellij.history.LocalHistory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.ActionInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import org.community.intellij.plugins.communitycase.commands.HandlerUtil;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.merge.MergeDialog;
import org.community.intellij.plugins.communitycase.merge.MergeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * "merge" action
 */
public class Merge extends RepositoryAction {

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  protected String getActionName() {
    return Bundle.getString("merge.action.name");
  }

  /**
   * {@inheritDoc}
   */
  protected void perform(@NotNull final Project project,
                         @NotNull final List<VirtualFile> Roots,
                         @NotNull final VirtualFile defaultRoot,
                         final Set<VirtualFile> affectedRoots,
                         final List<VcsException> exceptions) throws VcsException {
    MergeDialog dialog = new MergeDialog(project, Roots, defaultRoot);
    dialog.show();
    if (!dialog.isOK()) {
      return;
    }
    Label beforeLabel = LocalHistory.getInstance().putSystemLabel(project, "Before update");
    LineHandler h = dialog.handler();
    final VirtualFile root = dialog.getSelectedRoot();
    affectedRoots.add(root);
    VcsRevisionNumber currentRev = VcsRevisionNumber.resolve(project, root, "HEAD");
    try {
      HandlerUtil.doSynchronously(h, Bundle.message("merging.title", dialog.getSelectedRoot().getPath()), h.printableCommandLine());
    }
    finally {
      exceptions.addAll(h.errors());
    }
    if (exceptions.size() != 0) {
      return;
    }
    MergeUtil.showUpdates(this, project, exceptions, root, currentRev, beforeLabel, getActionName(), ActionInfo.INTEGRATE);
  }
}
