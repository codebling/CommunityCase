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
package org.community.intellij.plugins.communitycase.annotate;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.FileRevision;
import org.community.intellij.plugins.communitycase.RevisionNumber;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.history.HistoryUtils;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * annotation provider implementation.
 * <p/>
 * Based on the JetBrains SVNAnnotationProvider.
 */
public class IntellijAnnotationProvider implements com.intellij.openapi.vcs.annotate.AnnotationProvider {
  /**
   * the context project
   */
  private final Project myProject;
  /**
   * The author key for annotations
   */
  @NonNls private static final String AUTHOR_KEY = "author";
  /**
   * The committer time key for annotations
   */
  @NonNls private static final String COMMITTER_TIME_KEY = "committer-time";

  /**
   * A constructor
   *
   * @param project a context project
   */
  public IntellijAnnotationProvider(@NotNull Project project) {
    myProject = project;
  }

  /**
   * {@inheritDoc}
   */
  public FileAnnotation annotate(@NotNull VirtualFile file) throws VcsException {
    return annotate(file, null);
  }

  /**
   * {@inheritDoc}
   */
  public FileAnnotation annotate(@NotNull final VirtualFile file, final VcsFileRevision revision) throws VcsException {
    if (file.isDirectory()) {
      throw new VcsException("Cannot annotate a directory");
    }
    final FileAnnotation[] annotation = new FileAnnotation[1];
    final Exception[] exception = new Exception[1];
    Runnable command = new Runnable() {
      public void run() {
        final ProgressIndicator progress = ProgressManager.getInstance().getProgressIndicator();
        try {
          final FilePath currentFilePath = VcsUtil.getFilePath(file.getPath());
          final FilePath realFilePath;
          if (progress != null) {
            progress.setText(Bundle.message("getting.history", file.getName()));
          }
          final List<VcsFileRevision> revisions = HistoryUtils.history(myProject, currentFilePath);
          if (revision == null) {
            realFilePath = HistoryUtils.getLastCommitName(myProject, currentFilePath);
          }
          else {
            realFilePath = ((FileRevision)revision).getPath();
          }
          if (progress != null) {
            progress.setText(Bundle.message("computing.annotation", file.getName()));
          }
          final com.intellij.openapi.vcs.annotate.FileAnnotation result = annotate(realFilePath, revision, revisions, file);
          annotation[0] = result;
        }
        catch (Exception e) {
          exception[0] = e;
        }
      }
    };
    if (ApplicationManager.getApplication().isDispatchThread()) {
      ProgressManager.getInstance()
        .runProcessWithProgressSynchronously(command, Bundle.getString("annotate.action.name"), false, myProject);
    }
    else {
      command.run();
    }
    if (exception[0] != null) {
      throw new VcsException("Failed to annotate: " + exception[0], exception[0]);
    }
    return annotation[0];
  }

  /**
   * Calculate annotations
   *
   * @param repositoryFilePath the file path in the repository
   * @param revision           the revision to checkout
   * @param revisions          the revision list from history
   * @param file               a virtual file for the action
   * @return a file annotation object
   * @throws VcsException if there is a problem with running
   */
  private FileAnnotation annotate(final FilePath repositoryFilePath,
                                     final VcsFileRevision revision,
                                     final List<VcsFileRevision> revisions,
                                     final VirtualFile file) throws VcsException {
    SimpleHandler h = new SimpleHandler(myProject, Util.getRoot(repositoryFilePath), Command.ANNOTATE);
    h.setRemote(true);
    h.setStdoutSuppressed(true);
    h.setCharset(file.getCharset());
    h.addParameters("-p", "-l", "-t", "-M");
    if (revision == null) {
      h.addParameters("HEAD");
    }
    else {
      h.addParameters(revision.getRevisionNumber().asString());
    }
    h.endOptions();
    h.addRelativePaths(repositoryFilePath);
    String output = h.run();
    IntellijFileAnnotation annotation = new IntellijFileAnnotation(myProject, file, revision == null);
    class CommitInfo {
      Date date;
      String author;
      RevisionNumber revision;
    }
    HashMap<String, CommitInfo> commits = new HashMap<String, CommitInfo>();
    for (StringScanner s = new StringScanner(output); s.hasMoreData();) {
      // parse header line
      String commitHash = s.spaceToken();
      if (commitHash.equals(RevisionNumber.NOT_COMMITTED_HASH)) {
        commitHash = null;
      }
      s.spaceToken(); // skip revision line number
      int lineNum = Integer.parseInt(s.spaceToken());
      s.nextLine();
      // parse commit information
      CommitInfo commit = commits.get(commitHash);
      if (commit != null) {
        while (s.hasMoreData() && !s.startsWith('\t')) {
          s.nextLine();
        }
      }
      else {
        commit = new CommitInfo();
        while (s.hasMoreData() && !s.startsWith('\t')) {
          String key = s.spaceToken();
          String value = s.line();
          if (commitHash != null && AUTHOR_KEY.equals(key)) {
            commit.author = value;
          }
          if (commitHash != null && COMMITTER_TIME_KEY.equals(key)) {
            commit.date = Util.parseTimestamp(value);
            commit.revision = new RevisionNumber(commitHash, commit.date);
          }
        }
        commits.put(commitHash, commit);
      }
      // parse line
      if (!s.hasMoreData()) {
        // if the file is empty, the next line will not start with tab and it will be
        // empty.  
        continue;
      }
      s.skipChars(1);
      String line = s.line(true);
      annotation.appendLineInfo(commit.date, commit.revision, commit.author, line, lineNum);
    }
    annotation.addLogEntries(revisions);
    return annotation;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isAnnotationValid(VcsFileRevision rev) {
    return true;
  }
}
