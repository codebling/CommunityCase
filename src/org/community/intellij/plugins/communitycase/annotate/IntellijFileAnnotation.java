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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.annotate.AnnotationSourceSwitcher;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspectAdapter;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.DateFormatUtil;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.actions.ShowAllSubmittedFilesAction;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * file annotation implementation
 * <p/>
 * Based on the JetBrains SVNAnnotationProvider.
 */
public class IntellijFileAnnotation extends com.intellij.openapi.vcs.annotate.FileAnnotation {
  private final static Logger LOG = Logger.getInstance("#"+IntellijFileAnnotation.class.getName());

  /**
   * annotated content
   */
  private final StringBuffer myContentBuffer = new StringBuffer();
  /**
   * The currently annotated lines
   */
  private final ArrayList<LineInfo> myLines = new ArrayList<LineInfo>();
  /**
   * The project reference
   */
  private final Project myProject;
  private final VcsRevisionNumber myBaseRevision;
  /**
   * Map from revision numbers to revisions
   */
  private final Map<VcsRevisionNumber, VcsFileRevision> myRevisionMap = new HashMap<VcsRevisionNumber, VcsFileRevision>();

  /**
   * the virtual file for which annotations are generated
   */
  private final VirtualFile myFile;

  private final LineAnnotationAspect DATE_ASPECT = new AnnotationAspect(AnnotationAspect.DATE, true) {
    public String doGetValue(LineInfo info) {
      final Date date = info.getDate();
      return date == null ? "" : DateFormatUtil.formatPrettyDate(date);
    }
  };

  private final LineAnnotationAspect REVISION_ASPECT = new AnnotationAspect(AnnotationAspect.REVISION, false) {
    @Override
    protected String doGetValue(LineInfo lineInfo) {
      final VcsRevisionNumber revision = lineInfo.getRevision();
      return revision == null ? "" : String.valueOf(revision.asString());
    }
  };

  private final LineAnnotationAspect AUTHOR_ASPECT = new AnnotationAspect(AnnotationAspect.AUTHOR, true) {
    @Override
    protected String doGetValue(LineInfo lineInfo) {
      final String author = lineInfo.getAuthor();
      return author == null ? "" : author;
    }
  };
  private final Vcs myVcs;

  /**
   * A constructor
   *
   * @param project     the project of annotation provider
   * @param file        the root
   * @param monitorFlag if false the file system will not be listened for changes (used for annotated files from the repository).
   */
  public IntellijFileAnnotation(@NotNull final Project project, @NotNull VirtualFile file, final boolean monitorFlag, final VcsRevisionNumber revision) {
    super(project);
    myProject = project;
    myVcs = Vcs.getInstance(myProject);
    myFile = file;
    myBaseRevision = revision == null ? (myVcs.getDiffProvider().getCurrentRevision(file)) : revision;
  }

  /**
   * Add revisions to the list (from log)
   *
   * @param revisions revisions to add
   */
  public void addLogEntries(List<VcsFileRevision> revisions) {
    for (VcsFileRevision vcsFileRevision : revisions) {
      myRevisionMap.put(vcsFileRevision.getRevisionNumber(), vcsFileRevision);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void dispose() {
  }

  /**
   * {@inheritDoc}
   */
  public LineAnnotationAspect[] getAspects() {
    return new LineAnnotationAspect[]{REVISION_ASPECT, DATE_ASPECT, AUTHOR_ASPECT};
  }

  /**
   * {@inheritDoc}
   */
  public String getToolTip(final int lineNumber) {
    if (myLines.size() <= lineNumber || lineNumber < 0) {
      return "";
    }
    final LineInfo info = myLines.get(lineNumber);
    if (info == null) {
      return "";
    }
    VcsFileRevision fileRevision = myRevisionMap.get(info.getRevision());
    if (fileRevision != null) {
      return Bundle
        .message("annotation.tool.tip", info.getRevision().asString(), fileRevision.getAuthor(), fileRevision.getRevisionDate(),
                fileRevision.getCommitMessage());
    }
    else {
      return "";
    }
  }

  /**
   * {@inheritDoc}
   */
  public String getAnnotatedContent() {
    return myContentBuffer.toString();
  }

  /**
   * {@inheritDoc}
   */
  public List<VcsFileRevision> getRevisions() {
    final List<VcsFileRevision> result = new ArrayList<VcsFileRevision>(myRevisionMap.values());
    Collections.sort(result, new Comparator<VcsFileRevision>() {
      public int compare(final VcsFileRevision o1, final VcsFileRevision o2) {
        return -1 * o1.getRevisionNumber().compareTo(o2.getRevisionNumber());
      }
    });
    return result;
  }

  public boolean revisionsNotEmpty() {
    return ! myRevisionMap.isEmpty();
  }

  public AnnotationSourceSwitcher getAnnotationSourceSwitcher() {
    return null;
  }

  @Override
  public int getLineCount() {
    return myLines.size();
  }

  /**
   * {@inheritDoc}
   */
  public VcsRevisionNumber getLineRevisionNumber(final int lineNumber) {
    if (myLines.size() <= lineNumber || lineNumber < 0 || myLines.get(lineNumber) == null) {
      return null;
    }
    final LineInfo lineInfo = myLines.get(lineNumber);
    return lineInfo == null ? null : lineInfo.getRevision();
  }

  private boolean lineNumberCheck(int lineNumber) {
    return myLines.size() <= lineNumber || lineNumber < 0 || myLines.get(lineNumber) == null;
  }

  @Override
  public Date getLineDate(int lineNumber) {
    if (lineNumberCheck(lineNumber)) {
      return null;
    }
    final LineInfo lineInfo = myLines.get(lineNumber);
    return lineInfo == null ? null : lineInfo.getDate();
  }

  /**
   * Get revision number for the line.
   */
  public VcsRevisionNumber originalRevision(int lineNumber) {
    return getLineRevisionNumber(lineNumber);
  }

  /**
   * Append line info
   *
   * @param date       the revision date
   * @param revision   the revision number
   * @param author     the author
   * @param line       the line content
   * @param lineNumber the line number for revision
   * @throws VcsException in case when line could not be processed
   */
  public void appendLineInfo(final Date date,
                             final VcsRevisionNumber revision,
                             final String author,
                             final String line,
                             final long lineNumber) throws VcsException {
    int expectedLineNo = myLines.size() + 1;
    if (lineNumber != expectedLineNo) {
      throw new VcsException("Adding for info for line " + lineNumber + " but we are expecting it to be for " + expectedLineNo);
    }
    myLines.add(new LineInfo(date, revision, author));
    myContentBuffer.append(line);
  }

  public int getNumLines() {
    return myLines.size();
  }

  /**
   * Revision annotation aspect implementation
   */
  private abstract class AnnotationAspect extends LineAnnotationAspectAdapter {
    public AnnotationAspect(String id, boolean showByDefault) {
      super(id, showByDefault);
    }

    public String getValue(int lineNumber) {
      if (myLines.size() <= lineNumber || lineNumber < 0 || myLines.get(lineNumber) == null) {
        return "";
      }
      else {
        return doGetValue(myLines.get(lineNumber));
      }
    }

    protected abstract String doGetValue(LineInfo lineInfo);

    @Override
    protected void showAffectedPaths(int lineNum) {
      if (lineNum >= 0 && lineNum < myLines.size()) {
        final LineInfo info = myLines.get(lineNum);
        VcsFileRevision revision = myRevisionMap.get(info.getRevision());
        if (revision != null) {
          ShowAllSubmittedFilesAction.showSubmittedFiles(myProject, revision, myFile);
        }
      }
    }
  }

  /**
   * Line information
   */
  static class LineInfo {
    /**
     * date of the change
     */
    private final Date myDate;
    /**
     * revision number
     */
    private final VcsRevisionNumber myRevision;
    /**
     * the author of the change
     */
    private final String myAuthor;

    /**
     * A constructor
     *
     * @param date     date of the change
     * @param revision revision number
     * @param author   the author of the change
     */
    public LineInfo(final Date date, final VcsRevisionNumber revision, final String author) {
      myDate = date;
      myRevision = revision;
      myAuthor = author;
    }

    /**
     * @return the revision date
     */
    public Date getDate() {
      return myDate;
    }

    /**
     * @return the revision number
     */
    public VcsRevisionNumber getRevision() {
      return myRevision;
    }

    /**
     * @return the author of the change
     */
    public String getAuthor() {
      return myAuthor;
    }
  }

  public VirtualFile getFile() {
    return myFile;
  }

  @Nullable
  @Override
  public VcsRevisionNumber getCurrentRevision() {
    return myBaseRevision;
  }

  @Override
  public VcsKey getVcsKey() {
    return Vcs.getKey();
  }

  @Override
  public boolean isBaseRevisionChanged(VcsRevisionNumber number) {
    final VcsRevisionNumber currentCurrentRevision = myVcs.getDiffProvider().getCurrentRevision(myFile);
    return myBaseRevision != null && ! myBaseRevision.equals(currentCurrentRevision);
  }
}
