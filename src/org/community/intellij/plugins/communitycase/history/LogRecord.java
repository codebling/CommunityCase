/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.history.LogParser.LogOption;
import org.community.intellij.plugins.communitycase.history.wholeTree.AbstractHash;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * One record (commit information) returned by log output.
 * The access methods try heavily to return some default value if real is unavailable, for example, blank string is better than null.
 * BUT if one tries to get an option which was not specified to the LogParser, one will get null.
 * @see org.community.intellij.plugins.communitycase.history.LogParser
 */
class LogRecord {
  private final Map<LogParser.LogOption, String> myOptions;
  private final List<String> myPaths;
  private final List<List<String>> myParts;

  LogRecord(Map<LogParser.LogOption, String> options, List<String> paths, List<List<String>> parts) {
    myOptions = options;
    myPaths = paths;
    myParts = parts;
  }

  List<String> getPaths() {
    return myPaths;
  }

  public List<List<String>> getParts() {
    return myParts;
  }

  @NotNull
  public List<FilePath> getFilePaths(VirtualFile root) throws VcsException {
    List<FilePath> res = new ArrayList<FilePath>();
    String prefix = root.getPath() + "/";
    for (String strPath : getPaths()) {
      final String subPath = Util.unescapePath(strPath);
      final FilePath revisionPath = VcsUtil.getFilePathForDeletedFile(prefix + subPath, false);
      res.add(revisionPath);
    }
    return res;
  }

  private String lookup(LogParser.LogOption key) {
    return shortBuffer(myOptions.get(key));
  }

  // trivial access methods
  String getVersion() { return lookup(LogOption.VERSION); }
  String getUser() { return lookup(LogOption.USER); }
  String getActionNameAndDescription() { return lookup(LogOption.ACTION_NAME_DESC); }
  String getComment() { return lookup(LogOption.COMMENT); }

  // access methods with some formatting or conversion

  Date getDate() {
    return Util.parseTimestamp(myOptions.get(LogOption.TIME));
  }

  long getLongTimeStamp() {
    return getDate().getTime();
  }

  long getAuthorTimeStamp() {
    return Long.parseLong(myOptions.get(LogOption.TIME));
  }

  String getFullMessage() {
    return (getActionNameAndDescription() + ": " + getComment()).trim();
  }

  String[] getParentsShortHashes() {
    final String parents = lookup(LogOption.VERSION);
    if (parents.trim().length() == 0) return ArrayUtil.EMPTY_STRING_ARRAY;
    return parents.split(" ");
  }

  String[] getParentsHashes() {
    final String parents = lookup(LogOption.VERSION);
    if (parents.trim().length() == 0) return ArrayUtil.EMPTY_STRING_ARRAY;
    return parents.split(" ");
  }

  public Collection<String> getRefs() {
    final String decorate = myOptions.get(LogOption.VERSION);
    final String[] refNames = parseRefNames(decorate);
    final List<String> result = new ArrayList<String>(refNames.length);
    for (String refName : refNames) {
      result.add(shortBuffer(refName));
    }
    return result;
  }
  /*
   * Returns the list of tags and the list of branches.
   * A single method is used to return both, because they are returned together by and we don't want to parse them twice.
   * @return
   * @param allBranchesSet
   */
  /*Pair<List<String>, List<String>> getTagsAndBranches(SymbolicRefs refs) {
    final String decorate = myOptions.get(REF_NAMES);
    final String[] refNames = parseRefNames(decorate);
    final List<String> tags = refNames.length > 0 ? new ArrayList<String>() : Collections.<String>emptyList();
    final List<String> branches = refNames.length > 0 ? new ArrayList<String>() : Collections.<String>emptyList();
    for (String refName : refNames) {
      if (refs.contains(refName)) {
        // also some s can return ref name twice (like (HEAD, HEAD), so check we will show it only once)
        if (!branches.contains(refName)) {
          branches.add(shortBuffer(refName));
        }
      } else {
        if (!tags.contains(refName)) {
          tags.add(shortBuffer(refName));
        }
      }
    }
    return Pair.create(tags, branches);
  }*/

  private static String[] parseRefNames(final String decorate) {
    final int startParentheses = decorate.indexOf("(");
    final int endParentheses = decorate.indexOf(")");
    if ((startParentheses == -1) || (endParentheses == -1)) return ArrayUtil.EMPTY_STRING_ARRAY;
    final String refs = decorate.substring(startParentheses + 1, endParentheses);
    return refs.split(", ");
  }

  private static String shortBuffer(String raw) {
    if(raw==null)
      return null;
    else
      return new String(raw);
  }

  public List<Change> coolChangesParser(Project project, VirtualFile vcsRoot) throws VcsException {
    final List<Change> result = new ArrayList<Change>();
    final VcsRevisionNumber thisRevision=HistoryUtils.createUnvalidatedRevisionNumber(getVersion());
    final String[] parentsShortHashes = getParentsShortHashes();
    final List<AbstractHash> parents = new ArrayList<AbstractHash>(parentsShortHashes.length);
    for (String parentsShortHash : parentsShortHashes) {
      parents.add(AbstractHash.create(parentsShortHash));
    }
    final List<List<String>> parts = getParts();
    if (parts != null) {
      for (List<String> partsPart: parts) {
        result.add(parseChange(project, vcsRoot, parents, partsPart, thisRevision));
      }
    }
    return result;
  }

  private Change parseChange(final Project project, final VirtualFile vcsRoot, final List<AbstractHash> parents,
                             final List<String> parts, final VcsRevisionNumber thisRevision) throws VcsException {
    final ContentRevision before;
    final ContentRevision after;
    FileStatus status = null;
    final String path = parts.get(1);
    final List<VcsRevisionNumber> parentRevisions = new ArrayList<VcsRevisionNumber>(parents.size());
    for (AbstractHash parent : parents) {
      parentRevisions.add(HistoryUtils.createUnvalidatedRevisionNumber(parent.getString()));
    }

    switch (parts.get(0).charAt(0)) {
      case 'C':
      case 'A':
        before = null;
        status = FileStatus.ADDED;
        after = org.community.intellij.plugins.communitycase.ContentRevision.createRevision(vcsRoot, path, thisRevision, project, false, false);
        break;
      case 'U':
        status = FileStatus.MERGED_WITH_CONFLICTS;
      case 'M':
        if (status == null) {
          status = FileStatus.MODIFIED;
        }
        final FilePath filePath = org.community.intellij.plugins.communitycase.ContentRevision.createPath(vcsRoot, path, false, true);
        before = org.community.intellij.plugins.communitycase.ContentRevision.createMultipleParentsRevision(project, filePath, parentRevisions);
        after = org.community.intellij.plugins.communitycase.ContentRevision.createRevision(vcsRoot, path, thisRevision, project, false, false);
        break;
      case 'D':
        status = FileStatus.DELETED;
        final FilePath filePathDeleted = org.community.intellij.plugins.communitycase.ContentRevision.createPath(vcsRoot, path, true, true);
        before = org.community.intellij.plugins.communitycase.ContentRevision.createMultipleParentsRevision(project, filePathDeleted, parentRevisions);
        after = null;
        break;
      case 'R':
        status = FileStatus.MODIFIED;
        final FilePath filePathAfterRename = org.community.intellij.plugins.communitycase.ContentRevision.createPath(vcsRoot, parts.get(2), false, false);
        after = org.community.intellij.plugins.communitycase.ContentRevision.createMultipleParentsRevision(project, filePathAfterRename, parentRevisions);
        before = org.community.intellij.plugins.communitycase.ContentRevision.createRevision(vcsRoot, path, thisRevision, project, true, true);
        break;
      default:
        throw new VcsException("Unknown file status: " + Arrays.asList(parts));
    }
    return new Change(before, after, status);
  }
}
