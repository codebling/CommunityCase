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
package org.community.intellij.plugins.communitycase.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsOutgoingChangesProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.containers.Convertor;
import org.community.intellij.plugins.communitycase.Branch;
import org.community.intellij.plugins.communitycase.BranchesSearcher;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.history.HistoryUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OutgoingChangesProvider implements VcsOutgoingChangesProvider<CommittedChangeList> {
  private final static Logger LOG = Logger.getInstance("#org.community.intellij.plugins.communitycase.changes.OutgoingChangesProvider");
  private final Project myProject;

  public OutgoingChangesProvider(Project project) {
    myProject = project;
  }

  public Pair<VcsRevisionNumber, List<CommittedChangeList>> getOutgoingChanges(final VirtualFile vcsRoot, final boolean findRemote)
    throws VcsException {
    LOG.debug("getOutgoingChanges root: " + vcsRoot.getPath());
    final BranchesSearcher searcher = new BranchesSearcher(myProject, vcsRoot, findRemote);
    if (searcher.getLocal() == null || searcher.getRemote() == null) {
      return new Pair<VcsRevisionNumber, List<CommittedChangeList>>(null, Collections.<CommittedChangeList>emptyList());
    }
    final VcsRevisionNumber base = searcher.getLocal().getMergeBase(myProject, vcsRoot, searcher.getRemote());
    if (base == null) {
      return new Pair<VcsRevisionNumber, List<CommittedChangeList>>(null, Collections.<CommittedChangeList>emptyList());
    }
    final List<CommittedChangeList> lists = Util.getLocalCommittedChanges(myProject, vcsRoot, new Consumer<SimpleHandler>() {
      public void consume(final SimpleHandler handler) {
        handler.addParameters(base.asString() + "..HEAD");
      }
    });
    return new Pair<VcsRevisionNumber, List<CommittedChangeList>>(base, lists);
  }

  @Nullable
  public VcsRevisionNumber getMergeBaseNumber(final VirtualFile anyFileUnderRoot) throws VcsException {
    LOG.debug("getMergeBaseNumber parameter: " + anyFileUnderRoot.getPath());
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final VirtualFile root = vcsManager.getVcsRootFor(anyFileUnderRoot);
    if (root == null) {
      LOG.info("VCS root not found");
      return null;
    }

    final BranchesSearcher searcher = new BranchesSearcher(myProject, root, true);
    if (searcher.getLocal() == null || searcher.getRemote() == null) {
      LOG.info("local or remote not found");
      return null;
    }
    final VcsRevisionNumber base = searcher.getLocal().getMergeBase(myProject, root, searcher.getRemote());
    LOG.debug("found base: " + ((base == null) ? null : base.asString()));
    return base;
  }

  @NotNull
  public <U> Collection<U> whichAreOutgoingChanges(Collection<U> revisions,
                                                   Convertor<U, VcsRevisionNumber> convertor,
                                                   Convertor<U, FilePath> filePatchConvertor, VirtualFile vcsRoot) throws VcsException {
    final BranchesSearcher searcher = new BranchesSearcher(myProject, vcsRoot, true);
    final Branch target = searcher.getRemote();
    if (searcher.getLocal() == null || target == null) {
      return new ArrayList<U>(revisions); // no information, better strict approach
    }
    // get branches with commit
    final Collection<U> result = new ArrayList<U>(revisions);
    for (Iterator<U> iterator = result.iterator(); iterator.hasNext();) {
      final U t = iterator.next();
      final List<String> branches = new ArrayList<String>();
      // we do not use passed revision convertor since it returns just recent commit on repo
      final VcsRevisionNumber revision=HistoryUtils.getCurrentRevision(myProject, filePatchConvertor.convert(t));
      if (revision == null) continue; // will be true for new files; they are anyway outgoing 

      final String containingCommit = revision.asString();
      try {
        Branch.listAsStrings(myProject, vcsRoot, true, false, branches, containingCommit);
      }
      catch (VcsException e) {
        LOG.info("containingCommit = '" + containingCommit + "', current revision = '" + (revision == null ? null : revision.asString())
                 + "', file = " + filePatchConvertor.convert(t).getPath());
        LOG.info(e);
        throw e;
      }

      if (branches.contains(target.getName())) {
        iterator.remove();
      }
    }

    return result;
  }

  public Date getRevisionDate(VcsRevisionNumber revision) {
    return null; //todo wc implement me !  try implementing HistoryUtils.getRevisionDate(revision);
  }
}
