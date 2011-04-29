
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
package org.community.intellij.plugins.communitycase.history.browser;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.AsynchConsumer;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.Tag;
import org.community.intellij.plugins.communitycase.commands.FileUtils;
import org.community.intellij.plugins.communitycase.history.HistoryUtils;
import org.community.intellij.plugins.communitycase.history.wholeTree.CommitHashPlusParents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LowLevelAccessImpl implements LowLevelAccess {
  private final static Logger LOG = Logger.getInstance("#"+LowLevelAccessImpl.class.getName());
  private final Project myProject;
  private final VirtualFile myRoot;

  public LowLevelAccessImpl(final Project project, final VirtualFile root) {
    myProject = project;
    myRoot = root;
  }

  @Override
  public VirtualFile getRoot() {
    return myRoot;
  }

  public void loadHashesWithParents(final @NotNull Collection<String> startingPoints,
                                    @NotNull final Collection<ChangesFilter.Filter> filters,
                                    final AsynchConsumer<CommitHashPlusParents> consumer,
                                    Getter<Boolean> isCanceled, int useMaxCnt) throws VcsException {
    final List<String> parameters = new ArrayList<String>();
    for (ChangesFilter.Filter filter : filters) {
      filter.getCommandParametersFilter().applyToCommandLine(parameters);
    }

    if (! startingPoints.isEmpty()) {
      for (String startingPoint : startingPoints) {
        parameters.add(startingPoint);
      }
    } else {
      parameters.add("--all");
    }
    if (useMaxCnt > 0) {
      parameters.add("--max-count=" + useMaxCnt);
    }

    HistoryUtils.hashesWithParents(myProject, new FilePathImpl(myRoot), consumer, isCanceled, parameters.toArray(new String[parameters.size()]));
  }

  @Override
  public List<Commit> getCommitDetails(final Collection<String> commitIds, SymbolicRefs refs) throws VcsException {
    return HistoryUtils.commitsDetails(myProject, new FilePathImpl(myRoot), refs, commitIds);
  }

  public void loadCommits(final Collection<String> startingPoints, final Date beforePoint, final Date afterPoint,
                             final Collection<ChangesFilter.Filter> filtersIn, final AsynchConsumer<Commit> consumer,
                             int maxCnt, SymbolicRefs refs) throws VcsException {
    final Collection<ChangesFilter.Filter> filters = new ArrayList<ChangesFilter.Filter>(filtersIn);
    if (beforePoint != null) {
      filters.add(new ChangesFilter.BeforeDate(new Date(beforePoint.getTime() - 1)));
    }
    if (afterPoint != null) {
      filters.add(new ChangesFilter.AfterDate(afterPoint));
    }

    loadCommits(startingPoints, Collections.<String>emptyList(), filters, consumer, maxCnt, null, refs);
  }

  public SymbolicRefs getRefs() throws VcsException {
    final SymbolicRefs refs = new SymbolicRefs();
    loadAllTags(refs.getTags());
    final List<Branch> allBranches = new ArrayList<Branch>();
    final Branch current = Branch.list(myProject, myRoot, true, true, allBranches, null);
    for (Branch branch : allBranches) {
      if (branch.isRemote()) {
        String name = branch.getName();
        name = name.startsWith("remotes/") ? name.substring("remotes/".length()) : name;
        refs.addRemote(name);
      } else {
        refs.addLocal(branch.getName());
      }
    }
    refs.setCurrent(current);
    if (current != null) {
      refs.setTrackedRemote(current.getTrackedRemoteName(myProject, myRoot));
    }
    return refs;
  }

  public void loadCommits(final @NotNull Collection<String> startingPoints, @NotNull final Collection<String> endPoints,
                          @NotNull final Collection<ChangesFilter.Filter> filters,
                          @NotNull final AsynchConsumer<Commit> consumer,
                          int useMaxCnt,
                          Getter<Boolean> isCanceled, SymbolicRefs refs)
    throws VcsException {

    final List<String> parameters = new ArrayList<String>();
    if (useMaxCnt > 0) {
      parameters.add("--max-count=" + useMaxCnt);
    }

    for (ChangesFilter.Filter filter : filters) {
      filter.getCommandParametersFilter().applyToCommandLine(parameters);
    }
    
    if (! startingPoints.isEmpty()) {
      for (String startingPoint : startingPoints) {
        parameters.add(startingPoint);
      }
    } else {
      parameters.add("--all");
    }

    for (String endPoint : endPoints) {
      parameters.add("^" + endPoint);
    }

    HistoryUtils.historyWithLinks(myProject, new FilePathImpl(myRoot),
            refs, consumer, isCanceled, parameters.toArray(new String[parameters.size()]));
  }

  public List<String> getBranchesWithCommit(final ShaHash hash) throws VcsException {
    final List<String> result = new ArrayList<String>();
    Branch.listAsStrings(myProject, myRoot, true, true, result, hash.getValue());
    //Branch.listAsStrings(myProject, myRoot, true, false, result, hash.getValue());
    return result;
  }

  public Collection<String> getTagsWithCommit(final ShaHash hash) throws VcsException {
    final List<String> result = new ArrayList<String>();
    Tag.listAsStrings(myProject, myRoot, result, hash.getValue());
    return result;
  }

  @Nullable
  public Branch loadLocalBranches(Collection<String> sink) throws VcsException {
    return Branch.listAsStrings(myProject, myRoot, false, true, sink, null);
  }

  @Nullable
  public Branch loadRemoteBranches(Collection<String> sink) throws VcsException {
    return Branch.listAsStrings(myProject, myRoot, true, false, sink, null);
  }

  public void loadAllBranches(List<String> sink) throws VcsException {
    Branch.listAsStrings(myProject, myRoot, true, false, sink, null);
    Branch.listAsStrings(myProject, myRoot, false, true, sink, null);
  }

  public void loadAllTags(Collection<String> sink) throws VcsException {
    Tag.listAsStrings(myProject, myRoot, sink, null);
  }

  public void cherryPick(ShaHash hash) throws VcsException {
    FileUtils.cherryPick(myProject, myRoot, hash.getValue());
  }
}
