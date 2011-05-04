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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.ClearExplorerHandler;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * "revert" action
 */
public class ClearExplore extends BasicAction {
  private static final Logger log=Logger.getInstance("#"+ClearExplore.class.getName());
  private static final String NAME=Bundle.getString("clearExplore.action.name");

  public ClearExplore() {
    super(NAME);
  }

  @Override
  public boolean perform(@NotNull final Project project,Vcs vcs,@NotNull final List<VcsException> exceptions,@NotNull VirtualFile[] files) {
    if(isEnabled(project,vcs,files)) {
      new ClearExplorerHandler(project,Util.virtualFileToFile(files[0])).start();
      return true;
    }
    return false;
  }

  @Override
  @NotNull
  protected String getActionName() {
    return NAME;
  }

  @Override
  protected boolean isEnabled(@NotNull Project project,@NotNull Vcs vcs,@NotNull VirtualFile... vFiles) {
    if(vFiles.length!=1)
      return false;

    if(!vFiles[0].isDirectory())
      return false;

    FileStatus fileStatus=FileStatusManager.getInstance(project).getStatus(vFiles[0]);
    //noinspection SimplifiableIfStatement
    if(!(fileStatus!=FileStatus.IGNORED && fileStatus!=FileStatus.UNKNOWN))
      return false;

    return new ClearExplorerHandler(project,Util.virtualFileToFile(vFiles[0])).isValid();
  }
}
