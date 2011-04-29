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
package org.community.intellij.plugins.communitycase.update;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.SequentialUpdatesContext;
import com.intellij.openapi.vcs.update.UpdatedFiles;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.config.VcsProjectSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * update environment implementation. The environment does
 * {@code pull -v} for each vcs root. Rebase variant is detected
 * and processed as well.
 */
public class UpdateEnvironment implements com.intellij.openapi.vcs.update.UpdateEnvironment {
  /**
   * The vcs instance
   */
  private final Vcs myVcs;
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * The project settings
   */
  private final VcsProjectSettings mySettings;

  /**
   * A constructor from settings
   *
   * @param project a project
   */
  public UpdateEnvironment(@NotNull Project project, @NotNull Vcs vcs, VcsProjectSettings settings) {
    myVcs = vcs;
    myProject = project;
    mySettings = settings;
  }

  /**
   * {@inheritDoc}
   */
  public void fillGroups(UpdatedFiles updatedFiles) {
    //unused, there are no custom categories yet
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public com.intellij.openapi.vcs.update.UpdateSession updateDirectories(@NotNull FilePath[] filePaths,
                                         UpdatedFiles updatedFiles,
                                         ProgressIndicator progressIndicator,
                                         @NotNull Ref<SequentialUpdatesContext> sequentialUpdatesContextRef)
    throws ProcessCanceledException {
    Set<VirtualFile> roots = Util.roots(Arrays.asList(filePaths));
    List<VcsException> exceptions = new ArrayList<VcsException>();
    new UpdateProcess(myProject, mySettings, myVcs, updatedFiles, exceptions).doUpdate( progressIndicator, roots);
    return new UpdateSession(exceptions);
  }


  /**
   * {@inheritDoc}
   */
  public boolean validateOptions(Collection<FilePath> filePaths) {
    for (FilePath p : filePaths) {
      if (!Util.isUnder(p)) {
        return false;
      }
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public Configurable createConfigurable(Collection<FilePath> files) {
    return new UpdateConfigurable(mySettings);
  }

}
