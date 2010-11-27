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
package org.community.intellij.plugins.communitycase.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.community.intellij.plugins.communitycase.Vcs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Git VCS configurable implementation
 */
public class VcsConfigurable implements Configurable {
  private final VcsSettings settings;
  private VcsPanel panel;
  private final Project project;

  public VcsConfigurable(@NotNull VcsSettings settings, @NotNull Project project) {
    this.project = project;
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  public String getDisplayName() {
    return Vcs.NAME;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public Icon getIcon() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public String getHelpTopic() {
    return "project.propVCSSupport.VCSs.Git";
  }

  /**
   * {@inheritDoc}
   */
  public JComponent createComponent() {
    panel = new VcsPanel(project);
    panel.load(settings);
    return panel.getPanel();
  }

  /**
   * {@inheritDoc}
   */
  public boolean isModified() {
    return panel.isModified(settings);
  }

  /**
   * {@inheritDoc}
   */
  public void apply() throws ConfigurationException {
    panel.save(settings);
  }

  /**
   * {@inheritDoc}
   */
  public void reset() {
    panel.load(settings);
  }

  /**
   * {@inheritDoc}
   */
  public void disposeUIResources() {
  }
}
