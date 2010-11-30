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
 * The stash dialog.
 */
public class StashDialog extends DialogWrapper {
  private JComboBox myRootComboBox; // root selector
  private JPanel myRootPanel;
  private JLabel myCurrentBranch;
  private JTextField myMessageTextField;
  private JCheckBox myKeepIndexCheckBox; // --keep-index
  private final Project myProject;

  /**
   * A constructor
   *
   * @param project     the project
   * @param roots       the list of roots
   * @param defaultRoot the default root to select
   */
  public StashDialog(final Project project, final List<VirtualFile> roots, final VirtualFile defaultRoot) {
    super(project, true);
    myProject = project;
    setTitle(Bundle.getString("stash.title"));
    setOKButtonText(Bundle.getString("stash.button"));
    UiUtil.setupRootChooser(project, roots, defaultRoot, myRootComboBox, myCurrentBranch);
    init();
  }

  public LineHandler handler() {
    LineHandler handler = new LineHandler(myProject, getRoot(), Command.STASH);
    handler.addParameters("save");
    if (myKeepIndexCheckBox.isSelected()) {
      handler.addParameters("--keep-index");
    }
    final String msg = myMessageTextField.getText().trim();
    if (msg.length() != 0) {
      handler.addParameters(msg);
    }
    return handler;
  }

  /**
   * @return the selected root
   */
  public VirtualFile getRoot() {
    return (VirtualFile)myRootComboBox.getSelectedItem();
  }

  protected JComponent createCenterPanel() {
    return myRootPanel;
  }

  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  @Override
  protected String getHelpId() {
    return "reference.VersionControl.Stash";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myMessageTextField;
  }
}
