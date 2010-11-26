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

package org.community.intellij.plugins.communitycase.vfs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.HashSet;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * The tracker for configuration files, it tracks the following events:
 * <ul>
 * <li>Changes in configuration files: ./config and ~/.config</li>
 * </ul>
 * The tracker assumes that roots are configured correctly.
 */
public class ConfigTracker implements RootsListener {
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * The vcs object
   */
  private final version controlVcs myVcs;
  /**
   * The vcs manager that tracks content roots
   */
  private final ProjectLevelVcsManager myVcsManager;
  /**
   * The listener collection (managed by Vcs object since lifetime of this object is less than lifetime of Vcs)
   */
  private final ConfigListener myMulticaster;
  /**
   * The appeared roots that has been already reported as changed
   */
  private final HashSet<VirtualFile> myReportedRoots = new HashSet<VirtualFile>();
  /**
   * Local file system service
   */
  private final LocalFileSystem myLocalFileSystem;
  /**
   * The file listener
   */
  private final MyFileListener myFileListener;

  /**
   * The constructor
   *
   * @param project     the context project
   * @param vcs         the vcs object
   * @param multicaster the listener collection to use
   */
  public ConfigTracker(Project project, version controlVcs vcs, ConfigListener multicaster) {
    myProject = project;
    myVcs = vcs;
    myMulticaster = multicaster;
    myLocalFileSystem = LocalFileSystem.getInstance();
    myVcsManager = ProjectLevelVcsManager.getInstance(project);
    myVcs.addRootsListener(this);
    myFileListener = new MyFileListener();
    VirtualFileManager.getInstance().addVirtualFileListener(myFileListener);
    RootsChanged();
  }

  /**
   * This method is invoked when set of configured roots changed.
   */
  public void RootsChanged() {
    VirtualFile[] contentRoots = myVcsManager.getRootsUnderVcs(myVcs);
    if (contentRoots == null || contentRoots.length == 0) {
      return;
    }
    Set<VirtualFile> currentRootSet = version controlUtil.RootsForPaths(Arrays.asList(contentRoots));
    HashSet<VirtualFile> newRoots = new HashSet<VirtualFile>(currentRootSet);
    synchronized (myReportedRoots) {
      for (Iterator<VirtualFile> i = myReportedRoots.iterator(); i.hasNext();) {
        VirtualFile root = i.next();
        if (!root.isValid()) {
          i.remove();
        }
      }
      newRoots.removeAll(myReportedRoots);
      myReportedRoots.clear();
      myReportedRoots.addAll(currentRootSet);
    }
    for (VirtualFile root : newRoots) {
      VirtualFile config = root.findFileByRelativePath("./config");
      myMulticaster.configChanged(root, config);
    }
    // visit user home directory in order to notice .config changes later
    VirtualFile userHome = getUserHome();
    if (userHome != null) {
      userHome.getChildren();
    }
  }

  /**
   * @return user home directory
   */
  @Nullable
  private VirtualFile getUserHome() {
    return myLocalFileSystem.findFileByPath(System.getProperty("user.home"));
  }


  /**
   * Dispose the tracker removing all registered listeners
   */
  public void dispose() {
    myVcs.removeRootsListener(this);
    VirtualFileManager.getInstance().removeVirtualFileListener(myFileListener);
  }


  /**
   * The listener for the file system that checks if the configuration files are changed.
   * Note that events are checked in quite a shallow form. More radical events will cause
   * remapping of roots and RootsChanged() event will be delivered.
   */
  private class MyFileListener extends VirtualFileAdapter {

    /**
     * Check if the event affects configuration files in registered roots
     *
     * @param file the file to check
     */
    private void checkConfigAffected(VirtualFile file) {
      if (file.getName().equals(".config")) {
        VirtualFile userHome = getUserHome();
        VirtualFile parent = file.getParent();
        if (userHome != null && parent != null && parent.equals(userHome)) {
          HashSet<VirtualFile> allRoots;
          synchronized (myReportedRoots) {
            allRoots = new HashSet<VirtualFile>(myReportedRoots);
          }
          for (VirtualFile root : allRoots) {
            myMulticaster.configChanged(root, file);
          }
        }
        return;
      }
      VirtualFile base = version controlUtil.getPossibleBase(file, ".", "config");
      if (base != null) {
        boolean reported;
        synchronized (myReportedRoots) {
          reported = myReportedRoots.contains(base);
        }
        if (reported) {
          myMulticaster.configChanged(base, file);
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCreated(VirtualFileEvent event) {
      checkConfigAffected(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
      checkConfigAffected(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentsChanged(VirtualFileEvent event) {
      checkConfigAffected(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
      super.fileCopied(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
      String fileName = event.getFileName();
      VirtualFile newParent = event.getNewParent();
      VirtualFile oldParent = event.getOldParent();
      if (fileName.equals("config")) {
        checkParent(newParent);
        checkParent(oldParent);
      }
      if (fileName.equals(".config")) {
        VirtualFile userHome = getUserHome();
        if (userHome != null && (newParent.equals(userHome) || oldParent.equals(userHome))) {
          HashSet<VirtualFile> allRoots;
          synchronized (myReportedRoots) {
            allRoots = new HashSet<VirtualFile>(myReportedRoots);
          }
          VirtualFile config = userHome.findChild(".config");
          for (VirtualFile root : allRoots) {
            myMulticaster.configChanged(root, config);
          }
        }
      }
    }

    /**
     * Check parent and report event if it is one of reported roots
     *
     * @param parent the parent to check
     */
    private void checkParent(VirtualFile parent) {
      if (parent.getName().equals(".")) {
        VirtualFile base = parent.getParent();
        if (base != null) {
          boolean reported;
          synchronized (myReportedRoots) {
            reported = myReportedRoots.contains(base);
          }
          if (reported) {
            myMulticaster.configChanged(base, parent.findChild("config"));
          }
        }
      }
    }
  }
}
