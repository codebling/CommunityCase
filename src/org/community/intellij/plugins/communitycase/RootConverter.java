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

package org.community.intellij.plugins.communitycase;

import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.HashSet;

import java.util.ArrayList;
import java.util.List;

/**
 * The converter for the VCS roots
 */
public class RootConverter implements AbstractVcs.RootsConvertor {
  /**
   * The static instance
   */
  public static final RootConverter INSTANCE = new RootConverter();

  /**
   * {@inheritDoc}
   */
  public List<VirtualFile> convertRoots(List<VirtualFile> result) {
    // The method relies on the fact that VFS caches metadata about files,
    // so the query should be relatively fast and work mostly with in-memory structures.

    // todo useless?
    ArrayList<VirtualFile> roots = new ArrayList<VirtualFile>();
    HashSet<VirtualFile> listed = new HashSet<VirtualFile>();
    for (VirtualFile f : result) {
      VirtualFile r = Util.RootOrNull(f);
      if (r != null && listed.add(r)) {
        roots.add(r);
      }
    }
    return roots;
  }
}
