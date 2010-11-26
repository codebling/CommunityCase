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

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Remote;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.config.ConfigUtil;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for plugin user interface
 */
public class UiUtil {
  /**
   * Text containing in the label when there is no current branch
   */
  public static final String NO_CURRENT_BRANCH = Bundle.getString("common.no.active.branch");

  /**
   * A private constructor for utility class
   */
  private UiUtil() { }

  /**
   * Displays a "success"-notification.
   */
  public static void notifySuccess(Project project, String title, String description) {
    Notifications.Bus.notify(new Notification(Vcs.NOTIFICATION_GROUP_ID, title, description, NotificationType.INFORMATION), project);
  }

  /**
   * @return a list cell renderer for virtual files (it renders presentable URL)
   * @param listCellRenderer
   */
  public static ListCellRenderer getVirtualFileListCellRenderer(final ListCellRenderer listCellRenderer) {
    return new ListCellRendererWrapper<VirtualFile>(listCellRenderer) {
      @Override
      public void customize(final JList list, final VirtualFile file, final int index, final boolean selected, final boolean hasFocus) {
        setText(file == null || !file.isValid() ? "(invalid)" : file.getPresentableUrl());
      }
    };
  }

  /**
   * Get text field from combobox
   *
   * @param comboBox a combobox to examine
   * @return the text field reference
   */
  public static JTextField getTextField(JComboBox comboBox) {
    return (JTextField)comboBox.getEditor().getEditorComponent();
  }

  /**
   * Create list cell renderer for remotes. It shows both name and url and highlights the default
   * remote for the branch with bold.
   *
   *
   * @param defaultRemote a default remote
   * @param fetchUrl      if true, the fetch url is shown
   * @param listCellRenderer
   * @return a list cell renderer for virtual files (it renders presentable URL
   */
  public static ListCellRenderer getRemoteListCellRenderer(final String defaultRemote, final boolean fetchUrl,
                                                              final ListCellRenderer listCellRenderer) {
    return new ListCellRendererWrapper<Remote>(listCellRenderer) {
      @Override
      public void customize(final JList list, final Remote remote, final int index, final boolean selected, final boolean hasFocus) {
        final String text;
        if (remote == null) {
          text = Bundle.getString("util.remote.renderer.none");
        }
        else if (".".equals(remote.name())) {
          text = Bundle.getString("util.remote.renderer.self");
        }
        else {
          String key;
          if (defaultRemote != null && defaultRemote.equals(remote.name())) {
            key = "util.remote.renderer.default";
          }
          else {
            key = "util.remote.renderer.normal";
          }
          text = Bundle.message(key, remote.name(), fetchUrl ? remote.fetchUrl() : remote.pushUrl());
        }
        setText(text);
      }
    };
  }

  /**
   * Setup root chooser with specified elements and link selection to the current branch label.
   *
   * @param project            a context project
   * @param roots              roots for the project
   * @param defaultRoot        a default root
   * @param RootChooser     root selector
   * @param currentBranchLabel current branch label (might be null)
   */
  public static void setupRootChooser(@NotNull final Project project,
                                      @NotNull final List<VirtualFile> roots,
                                      @Nullable final VirtualFile defaultRoot,
                                      @NotNull final JComboBox RootChooser,
                                      @Nullable final JLabel currentBranchLabel) {
    for (VirtualFile root : roots) {
      RootChooser.addItem(root);
    }
    RootChooser.setRenderer(getVirtualFileListCellRenderer(RootChooser.getRenderer()));
    RootChooser.setSelectedItem(defaultRoot != null ? defaultRoot : roots.get(0));
    if (currentBranchLabel != null) {
      final ActionListener listener = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          try {
            VirtualFile root = (VirtualFile)RootChooser.getSelectedItem();
            assert root != null : "The root must not be null";
            Branch current = Branch.current(project, root);
            if (current == null) {
              currentBranchLabel.setText(NO_CURRENT_BRANCH);
            }
            else {
              currentBranchLabel.setText(current.getName());
            }
          }
          catch (VcsException ex) {
            Vcs.getInstance(project).showErrors(Collections.singletonList(ex), Bundle.getString("merge.retrieving.branches"));
          }
        }
      };
      listener.actionPerformed(null);
      RootChooser.addActionListener(listener);
    }
  }

  /**
   * Get root from the chooser
   *
   * @param RootChooser the chooser constructed with {@link #setupRootChooser(Project, List, VirtualFile, JComboBox, JLabel)}.
   * @return the current selection
   */
  public static VirtualFile getRootFromRootChooser(JComboBox RootChooser) {
    return (VirtualFile)RootChooser.getSelectedItem();
  }

  /**
   * Show error associated with the specified operation
   *
   * @param project   the project
   * @param ex        the exception
   * @param operation the operation name
   */
  public static void showOperationError(final Project project, final VcsException ex, @NonNls @NotNull final String operation) {
    showOperationError(project, operation, ex.getMessage());
  }

  /**
   * Show errors associated with the specified operation
   *
   * @param project   the project
   * @param exs       the exceptions to show
   * @param operation the operation name
   */
  public static void showOperationErrors(final Project project,
                                         final Collection<VcsException> exs,
                                         @NonNls @NotNull final String operation) {
    if (exs.size() == 1) {
      //noinspection ThrowableResultOfMethodCallIgnored
      showOperationError(project, operation, exs.iterator().next().getMessage());
    }
    else if (exs.size() > 1) {
      // TODO use dialog in order to show big messages
      StringBuilder b = new StringBuilder();
      for (VcsException ex : exs) {
        b.append(Bundle.message("errors.message.item", ex.getMessage()));
      }
      showOperationError(project, operation, Bundle.message("errors.message", b.toString()));
    }
  }

  /**
   * Show error associated with the specified operation
   *
   * @param project   the project
   * @param message   the error description
   * @param operation the operation name
   */
  public static void showOperationError(final Project project, final String operation, final String message) {
    Messages.showErrorDialog(project, message, Bundle.message("error.occurred.during", operation));
  }

  /**
   * Show errors on the tab
   *
   * @param project the context project
   * @param title   the operation title
   * @param errors  the errors to display
   */
  public static void showTabErrors(Project project, String title, List<VcsException> errors) {
    AbstractVcsHelper.getInstance(project).showErrors(errors, title);
  }

  /**
   * Setup remotes combobox. The default remote for the current branch is selected by default.
   * This method gets current branch for the project.
   *
   * @param project        the project
   * @param root           the root
   * @param remoteCombobox the combobox to update
   * @param fetchUrl       if true, the fetch url is shown instead of push url
   */
  public static void setupRemotes(final Project project, final VirtualFile root, final JComboBox remoteCombobox, final boolean fetchUrl) {
    Branch Branch = null;
    try {
      Branch = Branch.current(project, root);
    }
    catch (VcsException ex) {
      // ignore error
    }
    final String branch = Branch != null ? Branch.getName() : null;
    setupRemotes(project, root, branch, remoteCombobox, fetchUrl);

  }

  /**
   * Setup remotes combobox. The default remote for the current branch is selected by default.
   *
   * @param project        the project
   * @param root           the root
   * @param currentBranch  the current branch
   * @param remoteCombobox the combobox to update
   * @param fetchUrl       if true, the fetch url is shown for remotes, push otherwise
   */
  public static void setupRemotes(final Project project,
                                  final VirtualFile root,
                                  final String currentBranch,
                                  final JComboBox remoteCombobox,
                                  final boolean fetchUrl) {
    try {
      List<Remote> remotes = Remote.list(project, root);
      String remote = null;
      if (currentBranch != null) {
        remote = ConfigUtil.getValue(project, root, "branch." + currentBranch + ".remote");
      }
      remoteCombobox.setRenderer(getRemoteListCellRenderer(remote, fetchUrl, remoteCombobox.getRenderer()));
      Remote toSelect = null;
      remoteCombobox.removeAllItems();
      for (Remote r : remotes) {
        remoteCombobox.addItem(r);
        if (r.name().equals(remote)) {
          toSelect = r;
        }
      }
      if (toSelect != null) {
        remoteCombobox.setSelectedItem(toSelect);
      }
    }
    catch (VcsException e) {
      Vcs.getInstance(project).showErrors(Collections.singletonList(e), Bundle.getString("pull.retrieving.remotes"));
    }
  }

  /**
   * Checks state of the {@code checked} checkbox and if state is {@code checkedState} than to disable {@code changed}
   * checkbox and change its state to {@code impliedState}. When the {@code checked} checkbox changes states to other state,
   * than enable {@code changed} and restore its state. Note that the each checkbox should be implied by only one other checkbox.
   *
   * @param checked      the checkbox to monitor
   * @param checkedState the state that triggers disabling changed state
   * @param changed      the checkbox to change
   * @param impliedState the implied state of checkbox
   */
  public static void imply(final JCheckBox checked, final boolean checkedState, final JCheckBox changed, final boolean impliedState) {
    ActionListener l = new ActionListener() {
      Boolean previousState;

      public void actionPerformed(ActionEvent e) {
        if (checked.isSelected() == checkedState) {
          if (previousState == null) {
            previousState = changed.isSelected();
          }
          changed.setEnabled(false);
          changed.setSelected(impliedState);
        }
        else {
          changed.setEnabled(true);
          if (previousState != null) {
            changed.setSelected(previousState);
            previousState = null;
          }
        }
      }
    };
    checked.addActionListener(l);
    l.actionPerformed(null);
  }

  /**
   * Declares states for two checkboxes to be mutually exclusive. When one of the checkboxes goes to the specified state, other is
   * disabled and forced into reverse of the state (to prevent very fast users from selecting incorrect state or incorrect
   * initial configuration).
   *
   * @param first       the first checkbox
   * @param firstState  the state of the first checkbox
   * @param second      the second checkbox
   * @param secondState the state of the second checkbox
   */
  public static void exclusive(final JCheckBox first, final boolean firstState, final JCheckBox second, final boolean secondState) {
    ActionListener l = new ActionListener() {
      /**
       * One way check for the condition
       * @param checked the first to check
       * @param checkedState the state to match
       * @param changed the changed control
       * @param impliedState the implied state
       */
      private void check(final JCheckBox checked, final boolean checkedState, final JCheckBox changed, final boolean impliedState) {
        if (checked.isSelected() == checkedState) {
          changed.setSelected(impliedState);
          changed.setEnabled(false);
        }
        else {
          changed.setEnabled(true);
        }
      }

      /**
       * {@inheritDoc}
       */
      public void actionPerformed(ActionEvent e) {
        check(first, firstState, second, !secondState);
        check(second, secondState, first, !firstState);
      }
    };
    first.addActionListener(l);
    second.addActionListener(l);
    l.actionPerformed(null);
  }

  /**
   * Checks state of the {@code checked} checkbox and if state is {@code checkedState} than to disable {@code changed}
   * text field and clean it. When the {@code checked} checkbox changes states to other state,
   * than enable {@code changed} and restore its state. Note that the each text field should be implied by
   * only one other checkbox.
   *
   * @param checked      the checkbox to monitor
   * @param checkedState the state that triggers disabling changed state
   * @param changed      the checkbox to change
   */
  public static void implyDisabled(final JCheckBox checked, final boolean checkedState, final JTextField changed) {
    ActionListener l = new ActionListener() {
      String previousState;

      public void actionPerformed(ActionEvent e) {
        if (checked.isSelected() == checkedState) {
          if (previousState == null) {
            previousState = changed.getText();
          }
          changed.setEnabled(false);
          changed.setText("");
        }
        else {
          changed.setEnabled(true);
          if (previousState != null) {
            changed.setText(previousState);
            previousState = null;
          }
        }
      }
    };
    checked.addActionListener(l);
    l.actionPerformed(null);
  }

  /**
   * Handles a low-level execution exception.
   * Checks that executable is valid. If it is not, then shows proper notification with an option to fix the path to ClearCase executables.
   * If it's valid, then we don't know what could happen and just display the general error notification.
   */
  public static void checkExecutableAndShowNotification(final Project project, VcsException e) {
    if (Vcs.getInstance(project).getExecutableValidator().checkExecutableAndNotifyIfNeeded()) {
      Notification notification = new Notification(Vcs.NOTIFICATION_GROUP_ID, Bundle.getString("general.error"), e.getLocalizedMessage(), NotificationType.ERROR);
      Notifications.Bus.notify(notification, project);
    }
  }
}
