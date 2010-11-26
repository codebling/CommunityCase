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
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.rebase.InteractiveRebaseEditorHandler;
import org.community.intellij.plugins.communitycase.rebase.RebaseActionDialog;
import org.community.intellij.plugins.communitycase.rebase.RebaseUtils;
import org.jetbrains.annotations.NonNls;

import java.util.Iterator;
import java.util.List;

/**
 * Base class for rebase [--skip, --continue] actions and rebase operation
 */
public abstract class AbstractRebaseResumeAction extends RebaseActionBase {

  /**
   * {@inheritDoc}
   */
  protected LineHandler createHandler(Project project, List<VirtualFile> Roots, VirtualFile defaultRoot) {
    for (Iterator<VirtualFile> i = Roots.iterator(); i.hasNext();) {
      if (!RebaseUtils.isRebaseInTheProgress(i.next())) {
        i.remove();
      }
    }
    if (Roots.size() == 0) {
      Messages.showErrorDialog(project, Bundle.getString("rebase.action.no.root"), Bundle.getString("rebase.action.error"));
      return null;
    }
    final VirtualFile root;
    if (Roots.size() == 1) {
      root = Roots.get(0);
    }
    else {
      if (!Roots.contains(defaultRoot)) {
        defaultRoot = Roots.get(0);
      }
      RebaseActionDialog d = new RebaseActionDialog(project, getActionTitle(), Roots, defaultRoot);
      root = d.selectRoot();
      if (root == null) {
        return null;
      }
    }
    LineHandler h = new LineHandler(project, root, Command.REBASE);
    h.addParameters(getOptionName());
    return h;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void configureEditor(InteractiveRebaseEditorHandler editor) {
    editor.setRebaseEditorShown();
  }

  /**
   * @return title for rebase operation
   */
  @NonNls
  protected abstract String getOptionName();

  /**
   * @return title for root selection dialog
   */
  protected abstract String getActionTitle();
}
