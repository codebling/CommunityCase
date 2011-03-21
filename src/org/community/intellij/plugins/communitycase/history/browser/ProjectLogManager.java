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

import com.intellij.lifecycle.PeriodicalTasksCloser;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsListener;
import com.intellij.openapi.vcs.changes.committed.AbstractCalledLater;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentI;
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.history.wholeTree.Log;
import org.community.intellij.plugins.communitycase.history.wholeTree.LogFactoryService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ProjectLogManager {
  private final static Logger LOG = Logger.getInstance("#org.community.intellij.plugins.communitycase.history.browser.ProjectLogManager");
  private static final String CONTENT_KEY = "Log";

  private final Project myProject;
  private final ProjectLevelVcsManager myVcsManager;
  private final LogFactoryService myLogFactoryService;

  private final AtomicReference<Content> myCurrentContent;
  private final AtomicReference<Log> myLogRef;
  private VcsListener myListener;

  public static final Topic<CurrentBranchListener> CHECK_CURRENT_BRANCH =
            new Topic<CurrentBranchListener>("CHECK_CURRENT_BRANCH", CurrentBranchListener.class);
  private CurrentBranchListener myCurrentBranchListener;
  private MessageBusConnection myConnection;

  public ProjectLogManager(final Project project, final ProjectLevelVcsManager vcsManager, final LogFactoryService logFactoryService) {
    myProject = project;
    myVcsManager = vcsManager;
    myLogFactoryService = logFactoryService;
    myCurrentContent = new AtomicReference<Content>();
    myLogRef = new AtomicReference<Log>();

    myListener = new VcsListener() {
      public void directoryMappingChanged() {
        new AbstractCalledLater(myProject, ModalityState.NON_MODAL) {
          public void run() {
            recalculateWindows();
          }
        }.callMe();
      }
    };
    myCurrentBranchListener = new CurrentBranchListener() {
      public void consume(VirtualFile file) {
        /*final VirtualFile baseDir = myProject.getBaseDir();
        if (baseDir == null) return;
        final Map<VirtualFile, Content> currentState = myComponentsMap.get();
        for (VirtualFile virtualFile : currentState.keySet()) {
          if (Comparing.equal(virtualFile, file)) {
            final String title = getCaption(baseDir, virtualFile);
            final Content content = currentState.get(virtualFile);
            if (! Comparing.equal(title, content.getDisplayName())) {
              new AbstractCalledLater(myProject, ModalityState.NON_MODAL) {
                public void run() {
                  content.setDisplayName(title);
                }
              }.callMe();
            }
            return;
          }
        }*/
      }
    };
  }

  public static ProjectLogManager getInstance(final Project project) {
    return PeriodicalTasksCloser.getInstance().safeGetService(project, ProjectLogManager.class);
  }

  public void deactivate() {
    myVcsManager.removeVcsListener(myListener);
    if (myCurrentContent.get() != null) {
      final ChangesViewContentI cvcm = ChangesViewContentManager.getInstance(myProject);
      cvcm.removeContent(myCurrentContent.get());
    }
    if (myConnection != null) {
      myConnection.disconnect();
      myConnection = null;
    }
  }

  public void activate() {
    myVcsManager.addVcsListener(myListener);
    recalculateWindows();
    myConnection = myProject.getMessageBus().connect(myProject);
    myConnection.subscribe(CHECK_CURRENT_BRANCH, myCurrentBranchListener);
  }

  private void recalculateWindows() {
    final Vcs vcs = Vcs.getInstance(myProject);
    final VirtualFile[] roots = myVcsManager.getRootsUnderVcs(vcs);
    final List<VirtualFile> fileList = Arrays.asList(roots);

    final ChangesViewContentI cvcm = ChangesViewContentManager.getInstance(myProject);
    final Content currContent = myCurrentContent.get();
    if (currContent != null) {
      myLogRef.get().rootsChanged(fileList);
      return;
    }
    final Log Log = myLogFactoryService.createComponent();
    final ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    final Content content = contentFactory.createContent(Log.getVisualComponent(), CONTENT_KEY, false);
    content.setCloseable(false);
    cvcm.addContent(content);
    Disposer.register(content, Log);
    myLogRef.set(Log);

    myCurrentContent.set(content);
    Log.rootsChanged(fileList);
  }

  public interface CurrentBranchListener extends Consumer<VirtualFile> {
  }
}
