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

import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.community.intellij.plugins.communitycase.rollback.RollbackEnvironment;
import org.community.intellij.plugins.communitycase.ui.UiUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The dialog that displays locally modified files during update process
 */
public class UpdateLocallyModifiedDialog extends DialogWrapper {
  /**
   * The rescan button
   */
  private JButton myRescanButton;
  /**
   * The list of files to revert
   */
  private JList myFilesList;

  private JLabel myDescriptionLabel;
  /**
   * The root label
   */
  private JLabel myRoot;
  /**
   * The root panel
   */
  private JPanel myRootPanel;
  /**
   * The collection with locally modified files
   */
  private final List<String> myLocallyModifiedFiles;

  /**
   * The constructor
   *
   * @param project              the current project
   * @param root                 the vcs root
   * @param locallyModifiedFiles the collection of locally modified files to use
   */
  protected UpdateLocallyModifiedDialog(final Project project, final VirtualFile root, List<String> locallyModifiedFiles) {
    super(project, true);
    myLocallyModifiedFiles = locallyModifiedFiles;
    setTitle(Bundle.getString("update.locally.modified.title"));
    myRoot.setText(root.getPresentableUrl());
    myFilesList.setModel(new DefaultListModel());
    setOKButtonText(Bundle.getString("update.locally.modified.revert"));
    syncListModel();
    myRescanButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        myLocallyModifiedFiles.clear();
        try {
          scanFiles(project, root, myLocallyModifiedFiles);
        }
        catch (VcsException ex) {
          UiUtil.showOperationError(project, ex, "Checking for locally modified files");
        }
      }
    });
    myDescriptionLabel
      .setText(Bundle.message("update.locally.modified.message", ApplicationNamesInfo.getInstance().getFullProductName()));
    init();
  }

  /**
   * Refresh list model according to the current content of the collection
   */
  private void syncListModel() {
    DefaultListModel listModel = (DefaultListModel)myFilesList.getModel();
    listModel.removeAllElements();
    for (String p : myLocallyModifiedFiles) {
      listModel.addElement(p);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected JComponent createCenterPanel() {
    return myRootPanel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  /**
   * Scan working tree and detect locally modified files
   *
   * @param project the project to scan
   * @param root    the root to scan
   * @param files   the collection with files
   * @throws VcsException if there problem with running or working tree is dirty in unsupported way
   */
  private static void scanFiles(Project project, VirtualFile root, List<String> files) throws VcsException {
    String rootPath = root.getPath();
    SimpleHandler h = new SimpleHandler(project, root, Command.DIFF);
    h.addParameters("--name-status");
    h.setNoSSH(true);
    h.setStdoutSuppressed(true);
    StringScanner s = new StringScanner(h.run());
    while (s.hasMoreData()) {
      if (s.isEol()) {
        s.line();
        continue;
      }
      if (s.tryConsume("M\t")) {
        String path = rootPath + "/" + Util.unescapePath(s.line());
        files.add(path);
      }
      else {
        throw new VcsException("Working tree is dirty in unsupported way: " + s.line());
      }
    }
  }


  /**
   * Show the dialog if needed
   *
   * @param project the project
   * @param root    the vcs root
   * @return true if showing is not needed or operation completed successfully
   */
  public static boolean showIfNeeded(final Project project, final VirtualFile root) {
    final ArrayList<String> files = new ArrayList<String>();
    try {
      scanFiles(project, root, files);
      final AtomicBoolean rc = new AtomicBoolean(true);
      if (!files.isEmpty()) {
        com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
            public void run() {
                UpdateLocallyModifiedDialog d = new UpdateLocallyModifiedDialog(project, root, files);
                d.show();
                rc.set(d.isOK());
            }
        });
        if (rc.get()) {
          if (!files.isEmpty()) {
            revertFiles(project, root, files);
          }
        }
      }
      return rc.get();
    }
    catch (final VcsException e) {
      com.intellij.util.ui.UIUtil.invokeAndWaitIfNeeded(new Runnable() {
          public void run() {
              UiUtil.showOperationError(project, e, "Checking for locally modified files");
          }
      });
      return false;
    }
  }

  /**
   * UndoCheckout files from the list
   *
   * @param project the project
   * @param root    the vcs root
   * @param files   the files to revert
   */
  private static void revertFiles(Project project, VirtualFile root, ArrayList<String> files) throws VcsException {
    // TODO consider deleted files
    RollbackEnvironment rollback = RollbackEnvironment.getInstance(project);
    ArrayList<FilePath> list = new ArrayList<FilePath>(files.size());
    for (String p : files) {
      list.add(VcsUtil.getFilePath(p));
    }
    rollback.revert(root, list);
  }
}
