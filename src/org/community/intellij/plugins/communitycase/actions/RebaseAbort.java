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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.HandlerUtil;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.rebase.RebaseActionDialog;
import org.community.intellij.plugins.communitycase.rebase.RebaseUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Rebase abort action
 */
public class RebaseAbort extends RepositoryAction {

  /**
   * {@inheritDoc}
   */
  protected void perform(@NotNull Project project,
                         @NotNull List<VirtualFile> Roots,
                         @NotNull VirtualFile defaultRoot,
                         Set<VirtualFile> affectedRoots,
                         List<VcsException> exceptions) throws VcsException {
    // remote all roots where there are no rebase in progress
    for (Iterator<VirtualFile> i = Roots.iterator(); i.hasNext();) {
      if (!RebaseUtils.isRebaseInTheProgress(i.next())) {
        i.remove();
      }
    }
    if (Roots.size() == 0) {
      Messages.showErrorDialog(project, Bundle.getString("rebase.action.no.root"), Bundle.getString("rebase.action.error"));
      return;
    }
    final VirtualFile root;
    if (Roots.size() == 1) {
      root = Roots.get(0);
    }
    else {
      if (!Roots.contains(defaultRoot)) {
        defaultRoot = Roots.get(0);
      }
      RebaseActionDialog d = new RebaseActionDialog(project, getActionName(), Roots, defaultRoot);
      root = d.selectRoot();
      if (root == null) {
        return;
      }
    }
    affectedRoots.add(root);
    SimpleHandler h = new SimpleHandler(project, root, Command.REBASE);
    h.addParameters("--abort");
    HandlerUtil.doSynchronously(h, getActionName(), h.printableCommandLine());
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  protected String getActionName() {
    return Bundle.getString("rebase.abort.action.name");
  }
}
