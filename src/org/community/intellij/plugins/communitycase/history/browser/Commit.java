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

import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.changes.Change;
import org.community.intellij.plugins.communitycase.history.wholeTree.AbstractHash;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Commit {
  @NotNull
  private final AbstractHash myShortHash;
  @NotNull
  private final SHAHash myHash;
  private final String myAuthor;
  private final String myCommitter;
  private final String myDescription;
  private final Date myDate;

  private final String myAuthorEmail;
  private final String myComitterEmail;

  private final List<String> myTags;
  private final List<String> myLocalBranches;
  private final List<String> myRemoteBranches;

  private final Set<String> myParentsHashes;
  private final Set<Commit> myParentsLinks;

  // todo concern having
  private final List<FilePath> myPathsList;
  private final List<Change> myChanges;
  private String myCurrentBranch;

  private final long myAuthorTime;
  //private final List<String> myBranches;
  private boolean myOnLocal;
  // very expensive to calculate it massively, seems it wouldnt be shown
  private boolean myOnTracked;

  public Commit(@NotNull final AbstractHash shortHash,
                @NotNull final SHAHash hash,
                final String author,
                final String committer,
                final Date date,
                final String description,
                final Set<String> parentsHashes,
                final List<FilePath> pathsList,
                final String authorEmail,
                final String comitterEmail,
                List<String> tags,
                final List<String> localBranches,
                final List<String> remoteBranches,
                List<Change> changes,
                long authorTime) {
    myShortHash = shortHash;
    myAuthor = author;
    myCommitter = committer;
    myDate = date;
    myDescription = description;
    myHash = hash;
    myParentsHashes = parentsHashes;
    myPathsList = pathsList;
    myAuthorEmail = authorEmail;
    myComitterEmail = comitterEmail;
    myTags = tags;
    myChanges = changes;
    myLocalBranches = localBranches;
    myRemoteBranches = remoteBranches;
    myAuthorTime = authorTime;
    //myBranches = branches;
    myParentsLinks = new HashSet<Commit>();
  }

  public void addParentLink(final Commit commit) {
    myParentsLinks.add(commit);
  }

  public String getAuthor() {
    return myAuthor;
  }

  public String getCommitter() {
    return myCommitter;
  }

  public Date getDate() {
    return myDate;
  }

  public String getDescription() {
    return myDescription;
  }

  @NotNull
  public SHAHash getHash() {
    return myHash;
  }

  // todo think of interface
  public Set<String> getParentsHashes() {
    return myParentsHashes;
  }

  // todo think of interface
  public Set<Commit> getParentsLinks() {
    return myParentsLinks;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Commit Commit = (Commit)o;

    if (!myHash.equals(Commit.myHash)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myHash.hashCode();
  }

  public List<String> getTags() {
    return myTags;
  }

  public void orderTags(final Comparator<String> comparator) {
    Collections.sort(myTags, comparator);
  }

  public List<String> getLocalBranches() {
    return myLocalBranches;
  }

  public List<String> getRemoteBranches() {
    return myRemoteBranches;
  }

  public String getAuthorEmail() {
    return myAuthorEmail;
  }

  public String getCommitterEmail() {
    return myComitterEmail;
  }

  public List<FilePath> getPathsList() {
    return myPathsList;
  }

  @Override
  public String toString() {
    return myHash.getValue();
  }

  @NotNull
  public AbstractHash getShortHash() {
    return myShortHash;
  }

  public List<Change> getChanges() {
    return myChanges;
  }

  public void setCurrentBranch(String s) {
    myCurrentBranch = s;
  }

  public String getCurrentBranch() {
    return myCurrentBranch;
  }

  public long getAuthorTime() {
    return myAuthorTime;
  }

  public String getComitterEmail() {
    return myComitterEmail;
  }

  public boolean isOnLocal() {
    return myOnLocal;
  }

  public void setOnLocal(boolean onLocal) {
    myOnLocal = onLocal;
  }

  public boolean isOnTracked() {
    return myOnTracked;
  }

  public void setOnTracked(boolean onTracked) {
    myOnTracked = onTracked;
  }
}
