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

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.checkout.branches.BranchConfigurations;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Git VCS configuration panel
 */
public class VcsPanel {
  private JButton myTestButton; // Test git executable
  private JComponent myRootPanel;
  private TextFieldWithBrowseButton myGitField;
  private JTextField myBranchFilter;
  private JComboBox mySSHExecutableComboBox; // Type of SSH executable to use
  private JComboBox myConvertTextFilesComboBox; // The conversion policy
  private JCheckBox myAskBeforeConversionsCheckBox; // The confirmation checkbox
  private JCheckBox myEnableBranchesWidgetCheckBox; // if selected, the branches widget is enabled in the status bar
  private final Project myProject;
  private final VcsApplicationSettings myAppSettings;
  private final VcsSettings myProjectSettings;
  private static final String IDEA_SSH = ApplicationNamesInfo.getInstance().getProductName() + " " + Bundle.getString("vcs.config.ssh.mode.idea"); // IDEA ssh value
  private static final String NATIVE_SSH = Bundle.getString("vcs.config.ssh.mode.native"); // Native SSH value
  private static final String CRLF_CONVERT_TO_PROJECT = Bundle.getString("vcs.config.convert.project");
  private static final String CRLF_DO_NOT_CONVERT = Bundle.getString("vcs.config.convert.do.not.convert");

  /**
   * The constructor
   *
   * @param project the context project
   */
  public VcsPanel(@NotNull Project project) {
    myAppSettings = VcsApplicationSettings.getInstance();
    myProjectSettings = VcsSettings.getInstance(project);
    myProject = project;
    /*
    mySSHExecutableComboBox.addItem(IDEA_SSH);
    mySSHExecutableComboBox.addItem(NATIVE_SSH);
    mySSHExecutableComboBox.setSelectedItem(VcsSettings.isDefaultIdeaSsh() ? IDEA_SSH : NATIVE_SSH);
    mySSHExecutableComboBox
      .setToolTipText(Bundle.message("vcs.config.ssh.mode.tooltip", ApplicationNamesInfo.getInstance().getFullProductName()));
    myAskBeforeConversionsCheckBox.setSelected(myProjectSettings.askBeforeLineSeparatorConversion());
    */
    myTestButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testConnection();
      }
    });
    /*
    myConvertTextFilesComboBox.addItem(CRLF_DO_NOT_CONVERT);
    myConvertTextFilesComboBox.addItem(CRLF_CONVERT_TO_PROJECT);
    myConvertTextFilesComboBox.setSelectedItem(CRLF_CONVERT_TO_PROJECT);
    */
    myGitField.addBrowseFolderListener(Bundle.getString("find.title"), Bundle.getString("find.description"), project,
                                       new FileChooserDescriptor(true, false, false, false, false, false));
    //myEnableBranchesWidgetCheckBox.setSelected(BranchConfigurations.getInstance(myProject).isWidgetEnabled());
  }

  /**
   * Test availability of the connection
   */
  private void testConnection() {
    if (myAppSettings != null) {
      myAppSettings.setPathToExecutable(myGitField.getText());
    }
    final String s;
    try {
      s = Vcs.version(myProject);
    }
    catch (VcsException e) {
      Messages.showErrorDialog(myProject, e.getMessage(), Bundle.getString("find.error.title"));
      return;
    }
    if (Version.parse(s).isSupported()) {
      Messages.showInfoMessage(myProject,
              Bundle.message("find.success.message",Vcs.getInstance(myProject).version().toString()),
              Bundle.getString("find.success.title"));
    }
    else {
      Messages.showWarningDialog(myProject, Bundle.message("find.unsupported.message", s, Version.MIN),
                                 Bundle.getString("find.unsupported.title"));
    }
  }

  /**
   * @return the configuration panel
   */
  public JComponent getPanel() {
    return myRootPanel;
  }

  /**
   * Load settings into the configuration panel
   *
   * @param settings the settings to load
   */
  public void load(@NotNull VcsSettings settings) {
    myGitField.setText(settings.getAppSettings().getPathToExecutable());
    myBranchFilter.setText(settings.getAppSettings().getBranchFilter());
    /*mySSHExecutableComboBox.setSelectedItem(settings.isIdeaSsh() ? IDEA_SSH : NATIVE_SSH);
    myAskBeforeConversionsCheckBox.setSelected(settings.askBeforeLineSeparatorConversion());
    myConvertTextFilesComboBox.setSelectedItem(crlfPolicyItem(settings));
    myEnableBranchesWidgetCheckBox.setSelected(BranchConfigurations.getInstance(myProject).isWidgetEnabled());
    */
  }

  /**
   * Get crlf policy item from settings
   *
   * @param settings the settings object
   * @return the item in crlf combobox
   */
  static private String crlfPolicyItem(VcsSettings settings) {
    String crlf;
    switch (settings.getLineSeparatorsConversion()) {
      case NONE:
        crlf = CRLF_DO_NOT_CONVERT;
        break;
      case PROJECT_LINE_SEPARATORS:
        crlf = CRLF_CONVERT_TO_PROJECT;
        break;
      default:
        assert false : "Unknown crlf policy: " + settings.getLineSeparatorsConversion();
        crlf = null;
    }
    return crlf;
  }

  /**
   * Check if fields has been modified with respect to settings object
   *
   * @param settings the settings to load
   */
  public boolean isModified(@NotNull VcsSettings settings) {
    return !settings.getAppSettings().getPathToExecutable().equals(myGitField.getText())
            ||!settings.getAppSettings().getBranchFilter().equals(myBranchFilter.getText());
  }

  /**
   * Save configuration panel state into settings object
   *
   * @param settings the settings object
   */
  public void save(@NotNull VcsSettings settings) {
    settings.getAppSettings().setPathToExecutable(myGitField.getText());
    settings.getAppSettings().setBranchFilter(myBranchFilter.getText());
    /*settings.setIdeaSsh(IDEA_SSH.equals(mySSHExecutableComboBox.getSelectedItem()));
    Object policyItem = myConvertTextFilesComboBox.getSelectedItem();
    VcsSettings.ConversionPolicy conversionPolicy;
    if (CRLF_DO_NOT_CONVERT.equals(policyItem)) {
      conversionPolicy = VcsSettings.ConversionPolicy.NONE;
    }
    else if (CRLF_CONVERT_TO_PROJECT.equals(policyItem)) {
      conversionPolicy = VcsSettings.ConversionPolicy.PROJECT_LINE_SEPARATORS;
    }
    else {
      throw new IllegalStateException("Unknown selected CRLF policy: " + policyItem);
    }
    settings.setLineSeparatorsConversion(conversionPolicy);
    settings.setAskBeforeLineSeparatorConversion(myAskBeforeConversionsCheckBox.isSelected());
    */
    //BranchConfigurations.getInstance(myProject).setWidgetEnabled(myEnableBranchesWidgetCheckBox.isSelected());
  }
}
