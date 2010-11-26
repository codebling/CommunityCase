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
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Remote;
import org.community.intellij.plugins.communitycase.i18n.Bundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Current branch dialog
 */
public class CurrentBranchDialog extends DialogWrapper {
  /**
   * The selection used to indicate that the branch from the current repository is tracked
   */
  private static final String REMOTE_THIS = Bundle.getString("current.branch.tracked.remote.this");
  /**
   * The selection used to indicate that nothing is tracked
   */
  private static final String REMOTE_NONE = Bundle.getString("current.branch.tracked.remote.none");
  /**
   * The selection used to indicate that nothing is tracked
   */
  private static final String BRANCH_NONE = Bundle.getString("current.branch.tracked.branch.none");
  /**
   * The container panel
   */
  private JPanel myPanel;
  /**
   * root selector
   */
  private JComboBox myRoot;
  /**
   * The current branch
   */
  private JLabel myCurrentBranch;
  /**
   * The repository
   */
  private JComboBox myRepositoryComboBox;
  /**
   * The tracked branch
   */
  private JComboBox myBranchComboBox;
  /**
   * The branches to merge
   */
  private final List<Branch> myBranches = new ArrayList<Branch>();
  /**
   * The repository tracked for the current branch
   */
  private String myTrackedRepository;
  /**
   * The tracked branch
   */
  private String myTrackedBranch;
  /**
   * The current project for the dialog
   */
  private final Project myProject;

  /**
   * A constructor
   *
   * @param project     the context project
   * @param roots       the roots for the project
   * @param defaultRoot the default root
   * @throws VcsException if there is a problem with running
   */
  public CurrentBranchDialog(Project project, List<VirtualFile> roots, VirtualFile defaultRoot) throws VcsException {
    super(project, true);
    myProject = project;
    setTitle(Bundle.getString("current.branch.title"));
    setOKButtonText(Bundle.getString("current.branch.change.tracked"));
    UiUtil.setupRootChooser(project, roots, defaultRoot, myRoot, myCurrentBranch);
    myRoot.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          rootUpdated();
        }
        catch (VcsException ex) {
          UiUtil.showOperationError(myProject, ex, " config");
        }
      }
    });
    myRepositoryComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        trackedRemoteUpdated();
      }
    });
    myBranchComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        validateFields();
      }
    });
    rootUpdated();
    init();
  }

  /**
   * Update tracked branches for the root
   *
   * @throws VcsException if there is a problem with
   */
  private void rootUpdated() throws VcsException {
    VirtualFile root = getRoot();
    Branch current = Branch.current(myProject, root);
    myRepositoryComboBox.removeAllItems();
    myRepositoryComboBox.addItem(REMOTE_NONE);
    if (current != null) {
      myRepositoryComboBox.addItem(REMOTE_THIS);
      for (Remote r : Remote.list(myProject, root)) {
        myRepositoryComboBox.addItem(r.name());
      }
    }
    myTrackedRepository = current == null ? null : current.getTrackedRemoteName(myProject, root);
    if (myTrackedRepository == null) {
      myTrackedRepository = REMOTE_NONE;
    }
    else if (".".equals(myTrackedRepository)) {
      myTrackedRepository = REMOTE_THIS;
    }
    myTrackedBranch = current == null ? null : current.getTrackedBranchName(myProject, root);
    if (myTrackedBranch == null) {
      myTrackedBranch = BRANCH_NONE;
    }
    else if (myTrackedBranch.startsWith(Branch.REFS_HEADS_PREFIX)) {
      myTrackedBranch = myTrackedBranch.substring(Branch.REFS_HEADS_PREFIX.length());
    }
    myRepositoryComboBox.setSelectedItem(myTrackedRepository);
    myBranches.clear();
    Branch.list(myProject, root, true, true, myBranches, null);
    trackedRemoteUpdated();
  }

  /**
   * Update tracked remote
   */
  private void trackedRemoteUpdated() {
    String remote = getTrackedRemote();
    myBranchComboBox.removeAllItems();
    if (REMOTE_NONE.equals(remote)) {
      myBranchComboBox.addItem(BRANCH_NONE);
      myBranchComboBox.setSelectedItem(BRANCH_NONE);
    }
    else {
      if (REMOTE_THIS.equals(remote)) {
        for (Branch b : myBranches) {
          if (!b.isRemote()) {
            myBranchComboBox.addItem(b.getName());
          }
        }
      }
      else {
        String prefix = Branch.REFS_REMOTES_PREFIX + remote + "/";
        for (Branch b : myBranches) {
          if (b.isRemote()) {
            String name = b.getFullName();
            if (name.startsWith(prefix)) {
              myBranchComboBox.addItem(b.getFullName().substring(prefix.length()));
            }
          }
        }
      }
      if (myTrackedBranch != null) {
        // select the same branch for the remote if it exists
        myBranchComboBox.setSelectedItem(myTrackedBranch);
      }
    }
    validateFields();
  }

  /**
   * Specify new tracked branch
   *
   * @throws VcsException if there is a problem with calling
   */
  public void updateTrackedBranch() throws VcsException {
    String remote = getTrackedRemote();
    String branch = getTrackedBranch();
    if (remote.equals(REMOTE_NONE) || branch.equals(REMOTE_NONE)) {
      remote = null;
      branch = null;
    }
    else if (remote.equals(REMOTE_THIS)) {
      remote = ".";
    }
    Branch c = Branch.current(myProject, getRoot());
    if (c != null) {
      c.setTrackedBranch(myProject, getRoot(), remote, Branch.REFS_HEADS_PREFIX + branch);
    }
  }

  /**
   * @return the currently selected tracked remote ({@link #REMOTE_NONE} if no branch is tracked)
   */
  private String getTrackedRemote() {
    String remote = (String)myRepositoryComboBox.getSelectedItem();
    return remote == null ? REMOTE_NONE : remote;
  }

  /**
   * Validate fields and update tracked branch as result
   */
  private void validateFields() {
    if (getTrackedRemote().equals(myTrackedRepository) && getTrackedBranch().equals(myTrackedBranch)) {
      // nothing to change
      setOKActionEnabled(false);
    }
    else {
      setOKActionEnabled(true);
    }
  }

  /**
   * @return the currently selected tracked branch ({@link #BRANCH_NONE} if no branch is tracked)
   */
  private String getTrackedBranch() {
    String branch = (String)myBranchComboBox.getSelectedItem();
    return branch == null ? BRANCH_NONE : branch;
  }

  /**
   * @return the current root
   */
  private VirtualFile getRoot() {
    return UiUtil.getRootFromRootChooser(myRoot);
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
    return "reference.vcs..current.branch";
  }
}
