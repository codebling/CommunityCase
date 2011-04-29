package org.community.intellij.plugins.communitycase.config;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vcs.VcsException;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * VCS configuration panel
 */
public class VcsPanel {
  private JButton myTestButton; // Test git executable
  private JComponent myRootPanel;
  private TextFieldWithBrowseButton myPathToExecutable;
  private JTextField myBranchFilter;
  private JTextField myPathFilter;
  private JCheckBox myMakeBranchFilterAppwide;
  private JCheckBox myMakePathFilterAppwide;
  private final Project myProject;
  private final VcsSettings mySettings;
  private static final String CRLF_CONVERT_TO_PROJECT = Bundle.getString("vcs.config.convert.project");
  private static final String CRLF_DO_NOT_CONVERT = Bundle.getString("vcs.config.convert.do.not.convert");

  /**
   * The constructor
   *
   * @param project the context project
   */
  public VcsPanel(@NotNull Project project) {
    mySettings= VcsSettings.getInstance(project);
    myProject = project;
    myTestButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        testConnection();
      }
    });
    myPathToExecutable.addBrowseFolderListener(Bundle.getString("find.title"), Bundle.getString("find.description"), project,
                                       new FileChooserDescriptor(true, false, false, false, false, false));
    myMakeBranchFilterAppwide.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        swapBranchFilter();
      }
    });
    myMakePathFilterAppwide.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        swapPathFilter();
      }
    });
  }

  private void swapBranchFilter() {
    boolean global=myMakeBranchFilterAppwide.isSelected();
    mySettings.setBranchFilter(myBranchFilter.getText(),!global);
    mySettings.setBranchFilterAppwide(global);
    myBranchFilter.setText(mySettings.getBranchFilter());
  }
  private void swapPathFilter() {
    boolean global=myMakePathFilterAppwide.isSelected();
    mySettings.setPathFilter(myPathFilter.getText(),!global);
    mySettings.setPathFilterAppwide(global);
    myPathFilter.setText(mySettings.getPathFilter());
  }

  /**
   * Test availability of the connection
   */
  private void testConnection() {
    if (mySettings!= null) {
      mySettings.setPathToExecutable(myPathToExecutable.getText());
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
    myPathToExecutable.setText(settings.getPathToExecutable());
    myBranchFilter.setText(settings.getBranchFilter());
    myPathFilter.setText(settings.getPathFilter());
    myMakeBranchFilterAppwide.setSelected(settings.isBranchFilterAppwide());
    myMakePathFilterAppwide.setSelected(settings.isPathFilterAppwide());
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
    return !settings.getPathToExecutable().equals(myPathToExecutable.getText())
            ||!settings.getBranchFilter().equals(myBranchFilter.getText())
            ||!settings.getPathFilter().equals(myPathFilter.getText());
  }

  /**
   * Save configuration panel state into settings object
   *
   * @param settings the settings object
   */
  public void save(@NotNull VcsSettings settings) {
    settings.setPathToExecutable(myPathToExecutable.getText());
    settings.setBranchFilterAppwide(myMakeBranchFilterAppwide.isSelected());
    settings.setPathFilterAppwide(myMakePathFilterAppwide.isSelected());
    settings.setBranchFilter(myBranchFilter.getText(), myMakeBranchFilterAppwide.isSelected());
    settings.setPathFilter(myPathFilter.getText(), myMakePathFilterAppwide.isSelected());
  }
}
