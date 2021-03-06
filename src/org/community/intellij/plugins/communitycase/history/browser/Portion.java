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

import com.intellij.util.AsynchConsumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

// todo introduce synchronization here?
public class Portion implements AsynchConsumer<Commit> {
  private final Map<String,ShaHash> myNameToHash;
  private final Map<String, Integer> myHolder;
  private boolean myStartFound;

  // ordered
  private List<Commit> myOrdered;

  private final boolean myChildrenWasSet;

  private final MultiMap<String, Commit> myOrphanMap;

  private final Set<String> myUsers;

  // parents, w/out loaded commits; theoretically, those in myOrphan map todo check
  private final List<Commit> myRoots;
  // what was passed into log command
  private final List<Commit> myLeafs;
  @Nullable private final List<ShaHash> myStartingPoints;

  public Portion(@Nullable final List<ShaHash> startingPoints) {
    myStartingPoints = startingPoints;
    myChildrenWasSet = startingPoints == null || startingPoints.isEmpty();
    
    myNameToHash = new HashMap<String,ShaHash>();
    myHolder = new HashMap<String, Integer>();
    myOrdered = new ArrayList<Commit>();
    
    myRoots = new ArrayList<Commit>();
    myLeafs = new ArrayList<Commit>();

    myOrphanMap = new MultiMap<String, Commit>();
    myUsers = new HashSet<String>();
  }

  public void finished() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public Set<String> getUsers() {
    return myUsers;
  }

  public void consume(final Commit commit) {
    myUsers.add(commit.getAuthor());
    myUsers.add(commit.getCommitter());

    myOrdered.add(commit);
    myHolder.put(commit.getShortHash().getString(), myOrdered.size() - 1);
    final Collection<Commit> orphans = myOrphanMap.get(commit.getShortHash().getString());
    for (Commit orphan : orphans) {
      orphan.addParentLink(commit);
    }

    final List<String> referencies = commit.getLocalBranches();
    if (! referencies.isEmpty()) {
      final ShaHash hash = commit.getHash();
      for (String reference : referencies) {
        myNameToHash.put(reference, hash);
      }
    }
    final List<String> tags = commit.getTags();
    if (! tags.isEmpty()) {
      final ShaHash hash = commit.getHash();
      for (String reference : tags) {
        myNameToHash.put(reference, hash);
      }
    }

    final Set<String> parentHashes = commit.getParentsHashes();
    if (parentHashes.isEmpty()) {
      myStartFound = true;
    } else {
      for (String parentHash : parentHashes) {
        final Integer idx = myHolder.get(parentHash);
        if (idx != null) {
          final Commit parent = myOrdered.get(idx);
          commit.addParentLink(parent);
        } else {
          myOrphanMap.putValue(parentHash, commit);
        }
      }
    }
  }

  public boolean isStartFound() {
    return myStartFound;
  }

  @Nullable
  public Commit getLast() {
    return myOrdered.isEmpty() ? null : myOrdered.get(myOrdered.size() - 1);
  }

  // todo make simplier
  public List<Commit> getXFrom(final int idx, final int num) {
    final List<Commit> result = new ArrayList<Commit>();
    iterateFrom(idx, new Processor<Commit>() {
      public boolean process(Commit Commit) {
        result.add(Commit);
        return result.size() == num;
      }
    });
    return result;
  }

  public void iterateFrom(final int idx, final Processor<Commit> processor) {
    if ((idx < 0) || (idx > (myOrdered.size() - 1))) return;

    for (int i = idx; i < myOrdered.size(); i++) {
      final Commit commit = myOrdered.get(i);
      if (processor.process(commit)) return;
    }
  }

  public ShaHash getHashForReference(final String reference) {
    return myNameToHash.get(reference);
  }

  @Nullable
  public Commit getByHash(final String hash) {
    final ShaHash shaHash = new ShaHash(hash);
    final Integer idx = myHolder.get(shaHash);
    return idx == null ? null : myOrdered.get(idx);
  }
}
