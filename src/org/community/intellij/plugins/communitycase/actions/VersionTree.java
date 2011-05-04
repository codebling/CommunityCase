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
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * "revert" action
 */
public class VersionTree extends BasicAction {
  private static final Logger log=Logger.getInstance("#"+VersionTree.class.getName());
  private static final String NAME=Bundle.getString("versiontree.action.name");

  public VersionTree() {
    super(NAME);
  }

  @Override
  public boolean perform(@NotNull final Project project, Vcs vcs, @NotNull final List<VcsException> exceptions, @NotNull VirtualFile[] files) {
    for(VirtualFile vf:files) {
      FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(vf);
      if(fileStatus!=FileStatus.IGNORED && fileStatus!=FileStatus.UNKNOWN)
        try {
          VirtualFile root;
          root=Util.getRoot(vf);
          if(vf.isDirectory() && root.equals(vf))
            root=vf.getParent();
          //todo wc create a more lightweight handler to fire and forget this instead of wasting threads and other resources
          LineHandler handler=new LineHandler(project,root,Command.VERSION_TREE_GRAPHICAL);
          handler.endOptions();
          handler.addParameters(vf.getName()+"@@");
          handler.start();
          vf.refresh(false, false); //todo wc since line handler runs in a separate thread this needs to be done there...implement properly when we migrate Handlers to Commands.
        } catch(VcsException e) {
          exceptions.add(e);
          log.error(e);
        }
    }

    return true;
  }

  @Override
  @NotNull
  protected String getActionName() {
    return NAME;
  }

  @Override
  protected boolean isEnabled(@NotNull Project project, @NotNull Vcs vcs, @NotNull VirtualFile... vFiles) {
    for (VirtualFile file : vFiles) {
      FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(file);
      if(fileStatus!=FileStatus.IGNORED && fileStatus!=FileStatus.UNKNOWN)
        return true;
    }
    return false;
  }
}
