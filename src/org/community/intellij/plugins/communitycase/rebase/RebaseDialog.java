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
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Tag;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.config.ConfigUtil;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.merge.MergeUtil;
import org.community.intellij.plugins.communitycase.ui.ReferenceValidator;
import org.community.intellij.plugins.communitycase.ui.UiUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The dialog that allows initiating rebase activity
 */
public class RebaseDialog extends DialogWrapper {
  /**
   * root selector
   */
  private JComboBox myRootComboBox;
  /**
   * The selector for branch to rebase
   */
  private JComboBox myBranchComboBox;
  /**
   * The from branch combo box. This is used as base branch if different from onto branch
   */
  private JComboBox myFromComboBox;
  /**
   * The validation button for from branch
   */
  private JButton myFromValidateButton;
  /**
   * The onto branch combobox.
   */
  private JComboBox myOntoComboBox;
  /**
   * The validate button for onto branch
   */
  private JButton myOntoValidateButton;
  /**
   * Show tags in drop down
   */
  private JCheckBox myShowTagsCheckBox;
  /**
   * Merge strategy drop down
   */
  private JComboBox myMergeStrategyComboBox;
  /**
   * If selected, rebase is interactive
   */
  private JCheckBox myInteractiveCheckBox;
  /**
   * No merges are performed if selected.
   */
  private JCheckBox myDoNotUseMergeCheckBox;
  /**
   * The root panel of the dialog
   */
  private JPanel myPanel;
  /**
   * If selected, remote branches are shown as well
   */
  private JCheckBox myShowRemoteBranchesCheckBox;
  /**
   * Preserve merges checkbox
   */
  private JCheckBox myPreserveMergesCheckBox;
  /**
   * The current project
   */
  private final Project myProject;
  /**
   * The list of local branches
   */
  private final List<Branch> myLocalBranches = new ArrayList<Branch>();
  /**
   * The list of remote branches
   */
  private final List<Branch> myRemoteBranches = new ArrayList<Branch>();
  /**
   * The current branch
   */
  private Branch myCurrentBranch;
  /**
   * The tags
   */
  private final List<Tag> myTags = new ArrayList<Tag>();
  /**
   * The validator for onto field
   */
  private final ReferenceValidator myOntoValidator;
  /**
   * The validator for from field
   */
  private final ReferenceValidator myFromValidator;

  /**
   * A constructor
   *
   * @param project     a project to select
   * @param roots       a repository roots for the project
   * @param defaultRoot a guessed default root
   */
  public RebaseDialog(Project project, List<VirtualFile> roots, VirtualFile defaultRoot) {
    super(project, true);
    setTitle(Bundle.getString("rebase.title"));
    setOKButtonText(Bundle.getString("rebase.button"));
    init();
    myProject = project;
    final Runnable validateRunnable = new Runnable() {
      public void run() {
        validateFields();
      }
    };
    myOntoValidator = new ReferenceValidator(myProject, myRootComboBox, UiUtil.getTextField(myOntoComboBox), myOntoValidateButton,
                                                validateRunnable);
    myFromValidator = new ReferenceValidator(myProject, myRootComboBox, UiUtil.getTextField(myFromComboBox), myFromValidateButton,
                                                validateRunnable);
    UiUtil.setupRootChooser(myProject, roots, defaultRoot, myRootComboBox, null);
    myRootComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        validateFields();
      }
    });
    setupBranches();
    setupStrategy();
    validateFields();
  }

  public LineHandler handler() {
    LineHandler h = new LineHandler(myProject, Root(), Command.REBASE);
    h.setNoSSH(true);
    if (myInteractiveCheckBox.isSelected() && myInteractiveCheckBox.isEnabled()) {
      h.addParameters("-i");
    }
    h.addParameters("-v");
    if (!myDoNotUseMergeCheckBox.isSelected()) {
      if (myMergeStrategyComboBox.getSelectedItem().equals(MergeUtil.DEFAULT_STRATEGY)) {
        h.addParameters("-m");
      }
      else {
        h.addParameters("-s", myMergeStrategyComboBox.getSelectedItem().toString());
      }
    }
    if (myPreserveMergesCheckBox.isSelected()) {
      h.addParameters("-p");
    }
    String from = UiUtil.getTextField(myFromComboBox).getText();
    String onto = UiUtil.getTextField(myOntoComboBox).getText();
    if (from.length() == 0) {
      h.addParameters(onto);
    }
    else {
      h.addParameters("--onto", onto, from);
    }
    final String selectedBranch = (String)myBranchComboBox.getSelectedItem();
    if (myCurrentBranch != null && !myCurrentBranch.getName().equals(selectedBranch)) {
      h.addParameters(selectedBranch);
    }
    return h;
  }

  /**
   * Setup strategy
   */
  private void setupStrategy() {
    for (String s : MergeUtil.getMergeStrategies(1)) {
      myMergeStrategyComboBox.addItem(s);
    }
    myMergeStrategyComboBox.setSelectedItem(MergeUtil.DEFAULT_STRATEGY);
    myDoNotUseMergeCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myMergeStrategyComboBox.setEnabled(!myDoNotUseMergeCheckBox.isSelected());
      }
    });
  }


  /**
   * Validate fields
   */
  private void validateFields() {
    if (UiUtil.getTextField(myOntoComboBox).getText().length() == 0) {
      setErrorText(null);
      setOKActionEnabled(false);
      return;
    }
    else if (myOntoValidator.isInvalid()) {
      setErrorText(Bundle.getString("rebase.invalid.onto"));
      setOKActionEnabled(false);
      return;
    }
    if (UiUtil.getTextField(myFromComboBox).getText().length() != 0 && myFromValidator.isInvalid()) {
      setErrorText(Bundle.getString("rebase.invalid.from"));
      setOKActionEnabled(false);
      return;
    }
    if (RebaseUtils.isRebaseInTheProgress(Root())) {
      setErrorText(Bundle.getString("rebase.in.progress"));
      setOKActionEnabled(false);
      return;
    }
    setErrorText(null);
    setOKActionEnabled(true);
  }

  /**
   * Setup branch drop down.
   */
  private void setupBranches() {
    UiUtil.getTextField(myOntoComboBox).getDocument().addDocumentListener(new DocumentAdapter() {
      protected void textChanged(final DocumentEvent e) {
        validateFields();
      }
    });
    final ActionListener rootListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        loadRefs();
        updateBranches();
      }
    };
    final ActionListener showListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateOntoFrom();
      }
    };
    myShowRemoteBranchesCheckBox.addActionListener(showListener);
    myShowTagsCheckBox.addActionListener(showListener);
    rootListener.actionPerformed(null);
    myRootComboBox.addActionListener(rootListener);
    myBranchComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateTrackedBranch();
      }
    });
  }

  /**
   * Update branches when root changed
   */
  private void updateBranches() {
    myBranchComboBox.removeAllItems();
    for (Branch b : myLocalBranches) {
      myBranchComboBox.addItem(b.getName());
    }
    if (myCurrentBranch != null) {
      myBranchComboBox.setSelectedItem(myCurrentBranch.getName());
    }
    else {
      myBranchComboBox.setSelectedItem(0);
    }
    updateOntoFrom();
    updateTrackedBranch();
  }

  /**
   * Update onto and from comboboxes.
   */
  private void updateOntoFrom() {
    String onto = UiUtil.getTextField(myOntoComboBox).getText();
    String from = UiUtil.getTextField(myFromComboBox).getText();
    myFromComboBox.removeAllItems();
    myOntoComboBox.removeAllItems();
    for (Branch b : myLocalBranches) {
      myFromComboBox.addItem(b);
      myOntoComboBox.addItem(b);
    }
    if (myShowRemoteBranchesCheckBox.isSelected()) {
      for (Branch b : myRemoteBranches) {
        myFromComboBox.addItem(b);
        myOntoComboBox.addItem(b);
      }
    }
    if (myShowTagsCheckBox.isSelected()) {
      for (Tag t : myTags) {
        myFromComboBox.addItem(t);
        myOntoComboBox.addItem(t);
      }
    }
    UiUtil.getTextField(myOntoComboBox).setText(onto);
    UiUtil.getTextField(myFromComboBox).setText(from);
  }

  /**
   * Load tags and branches
   */
  private void loadRefs() {
    try {
      myLocalBranches.clear();
      myRemoteBranches.clear();
      myTags.clear();
      final VirtualFile root = Root();
      Branch.list(myProject, root, true, false, myLocalBranches, null);
      Branch.list(myProject, root, false, true, myRemoteBranches, null);
      Tag.list(myProject, root, myTags);
      myCurrentBranch = Branch.current(myProject, root);
    }
    catch (VcsException e) {
      UiUtil.showOperationError(myProject, e, " branch -a");
    }
  }

  /**
   * Update tracked branch basing on the currently selected branch
   */
  private void updateTrackedBranch() {
    try {
      final VirtualFile root = Root();
      String currentBranch = (String)myBranchComboBox.getSelectedItem();
      final Branch trackedBranch;
      if (currentBranch != null) {
        String remote = ConfigUtil.getValue(myProject, root, "branch." + currentBranch + ".remote");
        String merge = ConfigUtil.getValue(myProject, root, "branch." + currentBranch + ".merge");
        String name =
          (merge != null && merge.startsWith(Branch.REFS_HEADS_PREFIX)) ? merge.substring(Branch.REFS_HEADS_PREFIX.length()) : null;
        if (remote == null || merge == null || name == null) {
          trackedBranch = null;
        }
        else {
          if (remote.equals(".")) {
            trackedBranch = new Branch(name, false, false);
          }
          else {
            trackedBranch = new Branch(remote + "/" + name, false, true);
          }
        }
      }
      else {
        trackedBranch = null;
      }
      if (trackedBranch != null) {
        myOntoComboBox.setSelectedItem(trackedBranch);
      }
      else {
        UiUtil.getTextField(myOntoComboBox).setText("");
      }
      UiUtil.getTextField(myFromComboBox).setText("");
    }
    catch (VcsException e) {
      UiUtil.showOperationError(myProject, e, " config");
    }
  }

  /**
   * @return the currently selected root
   */
  public VirtualFile Root() {
    return (VirtualFile)myRootComboBox.getSelectedItem();
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
  protected JComponent createCenterPanel() {
    return myPanel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getHelpId() {
    return "reference.VersionControl.Rebase";
  }
}
