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
import com.intellij.openapi.options.ConfigurationException;
import org.community.intellij.plugins.communitycase.config.VcsProjectSettings;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

/**
 * Configurable for the update session
 */
public class UpdateConfigurable implements Configurable {
  /** The vcs settings for the configurable */
  private final VcsProjectSettings mySettings;
  /** The options panel */
  private UpdateOptionsPanel myPanel;

  /**
   * The constructor
   * @param settings the settings object
   */
  public UpdateConfigurable(VcsProjectSettings settings) {
    mySettings = settings;
  }

  /**
   * {@inheritDoc}
   */
  @Nls
  public String getDisplayName() {
    return Bundle.getString("update.options.display.name");
  }

  /**
   * {@inheritDoc}
   */
  public Icon getIcon() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String getHelpTopic() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public JComponent createComponent() {
    myPanel = new UpdateOptionsPanel();
    return myPanel.getPanel();  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * {@inheritDoc}
   */
  public boolean isModified() {
    return myPanel.isModified(mySettings);
  }

  /**
   * {@inheritDoc}
   */
  public void apply() throws ConfigurationException {
    myPanel.applyTo(mySettings);
  }

  /**
   * {@inheritDoc}
   */
  public void reset() {
    myPanel.updateFrom(mySettings);
  }

  /**
   * {@inheritDoc}
   */
  public void disposeUIResources() {
    myPanel = null;
  }
}
