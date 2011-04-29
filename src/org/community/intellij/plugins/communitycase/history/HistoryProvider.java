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
package org.community.intellij.plugins.communitycase.history;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.*;
import com.intellij.util.Consumer;
import com.intellij.util.ui.ColumnInfo;
import org.community.intellij.plugins.communitycase.FileRevision;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.actions.ShowAllSubmittedFilesAction;
import org.community.intellij.plugins.communitycase.config.ExecutableValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * history provider implementation
 */
public class HistoryProvider implements VcsHistoryProvider {
  /**
   * logger instance
   */
  private static final Logger log = Logger.getInstance("#"+HistoryProvider.class.getName());
  /**
   * the current project instance
   */
  private final Project myProject;

  /**
   * A constructor
   *
   * @param project a context project
   */
  public HistoryProvider(@NotNull Project project) {
    this.myProject = project;
  }

  /**
   * {@inheritDoc}
   */
  public VcsDependentHistoryComponents getUICustomization(final VcsHistorySession session, JComponent forShortcutRegistration) {
    return VcsDependentHistoryComponents.createOnlyColumns(new ColumnInfo[0]);
  }

  /**
   * {@inheritDoc}
   * @param refresher
   */
  public AnAction[] getAdditionalActions(Runnable refresher) {
    return new AnAction[]{new ShowAllSubmittedFilesAction(), new CopyHistoryRevisionNumberAction()};
  }

  /**
   * {@inheritDoc}
   */
  public boolean isDateOmittable() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public String getHelpId() {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public VcsHistorySession createSessionFor(final FilePath filePath) throws VcsException {
    List<VcsFileRevision> revisions = null;
    try {
      revisions = HistoryUtils.history(myProject, filePath);
    } catch (VcsException e) {
      Vcs.getInstance(myProject).getExecutableValidator().showNotificationOrThrow(e);
    }
    return createSession(filePath, revisions);
  }

  private VcsAbstractHistorySession createSession(final FilePath filePath, final List<VcsFileRevision> revisions) {
    return new VcsAbstractHistorySession(revisions) {
      @Nullable
      protected VcsRevisionNumber calcCurrentRevisionNumber() {
        try {
          return HistoryUtils.getCurrentRevision(myProject, HistoryUtils.getLastCommitName(myProject, filePath));
        }
        catch (VcsException e) {
          // likely the file is not under VCS anymore.
          if (log.isDebugEnabled()) {
            log.debug("Unable to retrieve the current revision number", e);
          }
          return null;
        }
      }

      public HistoryAsTreeProvider getHistoryAsTreeProvider() {
        return null;
      }

      @Override
      public VcsHistorySession copy() {
        return createSession(filePath, getRevisionList());
      }
    };
  }

  public void reportAppendableHistory(final FilePath path, final VcsAppendableHistorySessionPartner partner) throws VcsException {
    final VcsAbstractHistorySession emptySession = createSession(path, Collections.<VcsFileRevision>emptyList());
    partner.reportCreatedEmptySession(emptySession);
    final ExecutableValidator validator = Vcs.getInstance(myProject).getExecutableValidator();
    HistoryUtils.history(myProject, path, null, new Consumer<FileRevision>() {
        public void consume(FileRevision fileRevision) {
            partner.acceptRevision(fileRevision);
        }
    }, new Consumer<VcsException>() {
        public void consume(VcsException e) {
            if (validator.checkExecutableAndNotifyIfNeeded()) {
                partner.reportException(e);
            }
        }
    });
  }

  /**
   * {@inheritDoc}
   */
  public boolean supportsHistoryForDirectories() {
    return true;
  }
}
