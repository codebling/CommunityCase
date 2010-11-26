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
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.rebase.RebaseDialog;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * rebase action
 */
public class Rebase extends RebaseActionBase {

  /**
   * {@inheritDoc}
   */
  @NotNull
  protected String getActionName() {
    return Bundle.getString("rebase.action.name");
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  protected LineHandler createHandler(Project project, List<VirtualFile> Roots, VirtualFile defaultRoot) {
    RebaseDialog dialog = new RebaseDialog(project, Roots, defaultRoot);
    dialog.show();
    if (!dialog.isOK()) {
      return null;
    }
    return dialog.handler();
  }
}
