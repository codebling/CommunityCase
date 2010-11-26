/*
 * Copyright 2000-2010 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.history.wholeTree;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.changes.committed.AbstractCalledLater;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.SLRUMap;
import org.community.intellij.plugins.communitycase.history.browser.Commit;

import java.util.Collection;
import java.util.List;

/**
 * @author irengrig
 */
public class DetailsCache {
  private final static int ourSize = 400;
  private boolean mySomethingIsMissing;
  private final SLRUMap<Pair<VirtualFile, AbstractHash>, Commit> myCache;
  private final SLRUMap<Pair<VirtualFile, AbstractHash>, List<String>> myBranches;
  private final DetailsLoaderImpl myDetailsLoader;
  private final ModalityState myModalityState;
  private AbstractCalledLater myRefresh;
  private final Object myLock;

  public DetailsCache(final Project project, final UIRefresh uiRefresh, final DetailsLoaderImpl detailsLoader, final ModalityState modalityState) {
    myDetailsLoader = detailsLoader;
    myModalityState = modalityState;
    myRefresh = new AbstractCalledLater(project, myModalityState) {
      @Override
      public void run() {
        uiRefresh.detailsLoaded();
      }
    };
    myLock = new Object();
    mySomethingIsMissing = false;
    myCache = new SLRUMap<Pair<VirtualFile, AbstractHash>, Commit>(ourSize, 50);
    myBranches = new SLRUMap<Pair<VirtualFile, AbstractHash>, List<String>>(10, 10);
  }

  public Commit convert(final VirtualFile root, final AbstractHash hash) {
    synchronized (myLock) {
      return myCache.get(new Pair<VirtualFile, AbstractHash>(root, hash));
    }
  }

  public void acceptQuestion(final MultiMap<VirtualFile,AbstractHash> hashes) {
    if (hashes.isEmpty()) return;
    synchronized (myLock) {
      mySomethingIsMissing = ! hashes.isEmpty();
      myDetailsLoader.load(hashes);
    }
  }

  public void acceptAnswer(final Collection<Commit> commits, final VirtualFile root) {
    synchronized (myLock) {
      for (org.community.intellij.plugins.communitycase.history.browser.Commit commit : commits) {
        myCache.put(new Pair<VirtualFile, AbstractHash>(root, commit.getShortHash()), commit);
      }
      if (mySomethingIsMissing) {
        myRefresh.callMe();
        mySomethingIsMissing = false;
      }
    }
  }

  public void rootsChanged(final Collection<VirtualFile> roots) {
    myDetailsLoader.setRoots(roots);
  }

  public void putBranches(final VirtualFile root, final AbstractHash hash, final List<String> s) {
    myBranches.put(new Pair<VirtualFile, AbstractHash>(root, hash), s);
  }

  public List<String> getBranches(final VirtualFile root, final AbstractHash hash) {
    return myBranches.get(new Pair<VirtualFile, AbstractHash>(root, hash));
  }
}
