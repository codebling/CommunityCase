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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.HandlerUtil;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The tag dialog for the
 */
public class TagDialog extends DialogWrapper {
  /**
   * Root panel
   */
  private JPanel myPanel;
  /**
   * root selector
   */
  private JComboBox myRootComboBox;
  /**
   * Current branch label
   */
  private JLabel myCurrentBranch;
  /**
   * Tag name
   */
  private JTextField myTagNameTextField;
  /**
   * Force tag creation checkbox
   */
  private JCheckBox myForceCheckBox;
  /**
   * Text area that contains tag message if non-empty
   */
  private JTextArea myMessageTextArea;
  /**
   * The name of commit to tag
   */
  private JTextField myCommitTextField;
  /**
   * The validate button
   */
  private JButton myValidateButton;
  /**
   * The validator for commit text field
   */
  private final ReferenceValidator myCommitTextFieldValidator;
  /**
   * The current project
   */
  private final Project myProject;
  /**
   * Existing tags for the project
   */
  private final Set<String> myExistingTags = new HashSet<String>();
  /**
   * Prefix for message file name
   */
  @NonNls private static final String MESSAGE_FILE_PREFIX = "-tag-message-";
  /**
   * Suffix for message file name
   */
  @NonNls private static final String MESSAGE_FILE_SUFFIX = ".txt";
  /**
   * Encoding for the message file
   */
  @NonNls private static final String MESSAGE_FILE_ENCODING = "UTF-8";

  /**
   * A constructor
   *
   * @param project     a project to select
   * @param roots       a repository roots for the project
   * @param defaultRoot a guessed default root
   */
  public TagDialog(Project project, List<VirtualFile> roots, VirtualFile defaultRoot) {
    super(project, true);
    setTitle(Bundle.getString("tag.title"));
    setOKButtonText(Bundle.getString("tag.button"));
    myProject = project;
    UiUtil.setupRootChooser(myProject, roots, defaultRoot, myRootComboBox, myCurrentBranch);
    myRootComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        fetchTags();
        validateFields();
      }
    });
    fetchTags();
    myTagNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        validateFields();
      }
    });
    myCommitTextFieldValidator = new ReferenceValidator(project, myRootComboBox, myCommitTextField, myValidateButton, new Runnable() {
      public void run() {
        validateFields();
      }
    });
    myForceCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (myForceCheckBox.isEnabled()) {
          validateFields();
        }
      }
    });
    init();
    validateFields();
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myTagNameTextField;
  }

  /**
   * Perform tagging according to selected options
   *
   * @param exceptions the list where exceptions are collected
   */
  public void runAction(final List<VcsException> exceptions) {
    final String message = myMessageTextArea.getText();
    final boolean hasMessage = message.trim().length() != 0;
    final File messageFile;
    if (hasMessage) {
      try {
        messageFile = FileUtil.createTempFile(MESSAGE_FILE_PREFIX, MESSAGE_FILE_SUFFIX);
        messageFile.deleteOnExit();
        Writer out = new OutputStreamWriter(new FileOutputStream(messageFile), MESSAGE_FILE_ENCODING);
        try {
          out.write(message);
        }
        finally {
          out.close();
        }
      }
      catch (IOException ex) {
        Messages.showErrorDialog(myProject, Bundle.message("tag.error.creating.message.file.message", ex.toString()),
                                 Bundle.getString("tag.error.creating.message.file.title"));
        return;
      }
    }
    else {
      messageFile = null;
    }
    try {
      SimpleHandler h = new SimpleHandler(myProject, getRoot(), Command.TAG);
      h.setRemote(true);
      if (hasMessage) {
        h.addParameters("-a");
      }
      if (myForceCheckBox.isEnabled() && myForceCheckBox.isSelected()) {
        h.addParameters("-f");
      }
      if (hasMessage) {
        h.addParameters("-F", messageFile.getAbsolutePath());
      }
      h.addParameters(myTagNameTextField.getText());
      String object = myCommitTextField.getText().trim();
      if (object.length() != 0) {
        h.addParameters(object);
      }
      try {
        HandlerUtil.doSynchronously(h, Bundle.getString("tagging.title"), h.printableCommandLine());
        UiUtil.notifySuccess(myProject, myTagNameTextField.getText(), "Created tag " + myTagNameTextField.getText() + " successfully.");
      }
      finally {
        exceptions.addAll(h.errors());
      }
    }
    finally {
      if (messageFile != null) {
        //noinspection ResultOfMethodCallIgnored
        messageFile.delete();
      }
    }
  }

  /**
   * Validate dialog fields
   */
  private void validateFields() {
    String text = myTagNameTextField.getText();
    if (myExistingTags.contains(text)) {
      myForceCheckBox.setEnabled(true);
      if (!myForceCheckBox.isSelected()) {
        setErrorText(Bundle.getString("tag.error.tag.exists"));
        setOKActionEnabled(false);
        return;
      }
    }
    else {
      myForceCheckBox.setEnabled(false);
      myForceCheckBox.setSelected(false);
    }
    if (myCommitTextFieldValidator.isInvalid()) {
      setErrorText(Bundle.getString("tag.error.invalid.commit"));
      setOKActionEnabled(false);
      return;
    }
    if (text.length() == 0) {
      setErrorText(null);
      setOKActionEnabled(false);
      return;
    }
    setErrorText(null);
    setOKActionEnabled(true);
  }

  /**
   * Fetch tags
   */
  private void fetchTags() {
    myExistingTags.clear();
    SimpleHandler h = new SimpleHandler(myProject, getRoot(), Command.TAG);
    h.setRemote(true);
    h.setSilent(true);
    String output = HandlerUtil.doSynchronously(h, Bundle.getString("tag.getting.existing.tags"), h.printableCommandLine());
    for (StringScanner s = new StringScanner(output); s.hasMoreData();) {
      String line = s.line();
      if (line.length() == 0) {
        continue;
      }
      myExistingTags.add(line);
    }
  }

  /**
   * @return the current root
   */
  private VirtualFile getRoot() {
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
    return "reference.VersionControl.TagFiles";
  }
}
