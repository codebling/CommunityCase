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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.HandlerUtil;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.ui.UiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Initialize repository action
 */
public class Init extends DumbAwareAction {
  /**
   * {@inheritDoc}
   */
  public void actionPerformed(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    if (project == null) {
      return;
    }
    FileChooserDescriptor fcd = new FileChooserDescriptor(false, true, false, false, false, false);
    fcd.setShowFileSystemRoots(true);
    fcd.setTitle(Bundle.getString("init.destination.directory.title"));
    fcd.setDescription(Bundle.getString("init.destination.directory.description"));
    fcd.setHideIgnored(false);
    final VirtualFile baseDir = project.getBaseDir();
    final VirtualFile[] files = FileChooser.chooseFiles(project, fcd, baseDir);
    if (files.length == 0) {
      return;
    }
    final VirtualFile root = files[0];
    if (Util.isUnder(root)) {
      final int v = Messages.showYesNoDialog(project,
                                             Bundle.message("init.warning.already.under.",
                                                     StringUtil.escapeXml(root.getPresentableUrl())),
                                             Bundle.getString("init.warning.title"),
                                             Messages.getWarningIcon());
      if (v != 0) {
        return;
      }
    }
    LineHandler h = new LineHandler(project, root, Command.INIT);
    h.setNoSSH(true);
    HandlerUtil.doSynchronously(h, Bundle.getString("initializing.title"), h.printableCommandLine());
    if (!h.errors().isEmpty()) {
      UiUtil.showOperationErrors(project, h.errors(), " init");
      return;
    }
    int rc = Messages.showYesNoDialog(project, Bundle.getString("init.add.root.message"), Bundle.getString("init.add.root.title"),
                                      Messages.getQuestionIcon());
    if (rc != 0) {
      return;
    }
    final String path = root.equals(baseDir) ? "" : root.getPath();
    Vcs.getInstance(project).runInBackground(new Task.Backgroundable(project, Bundle.getString("common.refreshing")) {

      public void run(@NotNull ProgressIndicator indicator) {
        refreshAndConfigureVcsMappings(project, root, path);
      }
    });
  }

  public static void refreshAndConfigureVcsMappings(final Project project, final VirtualFile root, final String path) {
    root.refresh(false, false);
    ProjectLevelVcsManager vcs = ProjectLevelVcsManager.getInstance(project);
    final List<VcsDirectoryMapping> vcsDirectoryMappings = new ArrayList<VcsDirectoryMapping>(vcs.getDirectoryMappings());
    VcsDirectoryMapping mapping = new VcsDirectoryMapping(path, Vcs.getInstance(project).getName());
    for (int i = 0; i < vcsDirectoryMappings.size(); i++) {
      final VcsDirectoryMapping m = vcsDirectoryMappings.get(i);
      if (m.getDirectory().equals(path)) {
        if (m.getVcs().length() == 0) {
          vcsDirectoryMappings.set(i, mapping);
          mapping = null;
          break;
        }
        else if (m.getVcs().equals(mapping.getVcs())) {
          mapping = null;
          break;
        }
      }
    }
    if (mapping != null) {
      vcsDirectoryMappings.add(mapping);
    }
    vcs.setDirectoryMappings(vcsDirectoryMappings);
    vcs.updateActiveVcss();
    Util.refreshFiles(project, Collections.singleton(root));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void update(AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    Presentation presentation = e.getPresentation();
    if (project == null) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }
    presentation.setEnabled(true);
    presentation.setVisible(true);
  }
}
