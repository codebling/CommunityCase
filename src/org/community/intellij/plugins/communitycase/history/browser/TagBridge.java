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

import com.intellij.openapi.vcs.VcsException;
import com.intellij.util.containers.MultiMap;

import java.util.Collection;
import java.util.Set;

// when you're not at first page, or when your filter moved tags out, you might still want to filter by them
public class TagBridge {
  final Set<ShaHash> myParentsInterestedIn;
  private final LowLevelAccess myAccess;

  final MultiMap<ShaHash, String> myTagsForHashes;
  final MultiMap<String,ShaHash> myHashesForTags;

  public TagBridge(final Set<ShaHash> parentsInterestedIn, final LowLevelAccess access) {
    myParentsInterestedIn = parentsInterestedIn;
    myAccess = access;
    myTagsForHashes = new MultiMap<ShaHash, String>();
    myHashesForTags = new MultiMap<String,ShaHash>();
  }

  public void load() throws VcsException {
    for (ShaHash hash : myParentsInterestedIn) {
      final Collection<String> refs = myAccess.getBranchesWithCommit(hash);
      refs.addAll(myAccess.getTagsWithCommit(hash));

      myTagsForHashes.put(hash, refs);
      for (String ref : refs) {
        myHashesForTags.putValue(ref, hash);
      }
    }
  }

  // todo +-
  public MultiMap<String,ShaHash> getHashesForTags() {
    return myHashesForTags;
  }

  // todo +-
  public MultiMap<ShaHash, String> getTagsForHashes() {
    return myTagsForHashes;
  }
}
