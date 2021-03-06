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
package org.community.intellij.plugins.communitycase.rebase;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.config.ConfigUtil;
import org.community.intellij.plugins.communitycase.i18n.Bundle;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * The dialog used for the unstructured information from rebase.
 */
public class RebaseUnstructuredEditor extends DialogWrapper {
  /**
   * The text with information from the
   */
  private JTextArea myTextArea;
  /**
   * The root panel of the dialog
   */
  private JPanel myPanel;
  /**
   * The label that contains the root path
   */
  private JLabel myRootLabel;
  /**
   * The file encoding
   */
  private final String encoding;
  /**
   * The file being edited
   */
  private final File myFile;

  /**
   * The constructor
   *
   * @param project the context project
   * @param root    the root
   * @param path    the path to edit
   * @throws IOException if there is an IO problem
   */
  protected RebaseUnstructuredEditor(Project project, VirtualFile root, String path) throws IOException {
    super(project, true);
    setTitle(Bundle.message("rebase.unstructured.editor.title"));
    setOKButtonText(Bundle.message("rebase.unstructured.editor.button"));
    myRootLabel.setText(root.getPresentableUrl());
    encoding = ConfigUtil.getCommitEncoding(project, root);
    myFile = new File(path);
    myTextArea.setText(new String(FileUtil.loadFileText(myFile, encoding)));
    init();
  }

  /**
   * Save content to the file
   *
   * @throws IOException if there is an IO problem
   */
  public void save() throws IOException {
    FileUtil.writeToFile(myFile, myTextArea.getText().getBytes(encoding));
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
}
