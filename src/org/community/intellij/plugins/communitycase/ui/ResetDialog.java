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
package org.community.intellij.plugins.communitycase.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.i18n.Bundle;

import javax.swing.*;
import java.util.List;

/**
 * The dialog for the " reset" operation
 */
public class ResetDialog extends DialogWrapper {
  /**
   * The --soft reset type
   */
  private static final String SOFT = Bundle.getString("reset.type.soft");
  /**
   * The --mixed reset type
   */
  private static final String MIXED = Bundle.getString("reset.type.mixed");
  /**
   * The --hard reset type
   */
  private static final String HARD = Bundle.getString("reset.type.hard");
  /**
   * root selector
   */
  private JComboBox myRootComboBox;
  /**
   * The label for the current branch
   */
  private JLabel myCurrentBranchLabel;
  /**
   * The selector for reset type
   */
  private JComboBox myResetTypeComboBox;
  /**
   * The text field that contains commit expressions
   */
  private JTextField myCommitTextField;
  /**
   * The validate button
   */
  private JButton myValidateButton;
  /**
   * The root panel for the dialog
   */
  private JPanel myPanel;

  /**
   * The project
   */
  private final Project myProject;
  /**
   * The validator for commit text
   */
  private final ReferenceValidator myReferenceValidator;

  /**
   * A constructor
   *
   * @param project     the project
   * @param roots       the list of the roots
   * @param defaultRoot the default root to select
   */
  public ResetDialog(final Project project, final List<VirtualFile> roots, final VirtualFile defaultRoot) {
    super(project, true);
    myProject = project;
    setTitle(Bundle.getString("reset.title"));
    setOKButtonText(Bundle.getString("reset.button"));
    myResetTypeComboBox.addItem(MIXED);
    myResetTypeComboBox.addItem(SOFT);
    myResetTypeComboBox.addItem(HARD);
    myResetTypeComboBox.setSelectedItem(MIXED);
    UiUtil.setupRootChooser(project, roots, defaultRoot, myRootComboBox, myCurrentBranchLabel);
    myReferenceValidator = new ReferenceValidator(myProject, myRootComboBox, myCommitTextField, myValidateButton, new Runnable() {
      public void run() {
        validateFields();
      }
    });
    init();
  }

  /**
   * Validate
   */
  void validateFields() {
    if (myReferenceValidator.isInvalid()) {
      setErrorText(Bundle.getString("reset.commit.invalid"));
      setOKActionEnabled(false);
    }
    setErrorText(null);
    setOKActionEnabled(true);
  }

  /**
   * @return the handler for reset operation
   */
  public LineHandler handler() {
    LineHandler handler = new LineHandler(myProject, getRoot(), Command.RESET);
    handler.setNoSSH(true);
    String type = (String)myResetTypeComboBox.getSelectedItem();
    if (SOFT.equals(type)) {
      handler.addParameters("--soft");
    }
    else if (HARD.equals(type)) {
      handler.addParameters("--hard");
    }
    else if (MIXED.equals(type)) {
      handler.addParameters("--mixed");
    }
    final String commit = myCommitTextField.getText().trim();
    if (commit.length() != 0) {
      handler.addParameters(commit);
    }
    return handler;
  }

  /**
   * @return the selected root
   */
  public VirtualFile getRoot() {
    return (VirtualFile)myRootComboBox.getSelectedItem();
  }

  /**
   * {@inheritDoc}
   */
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getHelpId() {
    return "ResetHead";
  }
}
