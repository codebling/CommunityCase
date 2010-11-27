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

import com.intellij.ProjectTopics;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandAdapter;
import com.intellij.openapi.command.CommandEvent;
import com.intellij.openapi.command.CommandListener;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDirectoryMapping;
import com.intellij.openapi.vcs.VcsListener;
import com.intellij.openapi.vfs.*;
import com.intellij.openapi.vfs.ex.VirtualFileManagerAdapter;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The component tracks roots for the project. If roots are mapped incorrectly it
 * shows balloon that notifies user about the problem and offers to correct root mapping.
 */
public class RootTracker implements VcsListener {
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * Tracker of roots for project root manager
   */
  private final ProjectRootManager myProjectRoots;
  /**
   * The vcs manager that tracks content roots
   */
  private final ProjectLevelVcsManager myVcsManager;
  /**
   * The vcs instance
   */
  private final Vcs myVcs;
  /**
   * If true, the tracking is enabled.
   */
  private final AtomicBoolean myIsEnabled = new AtomicBoolean(false);
  /**
   * If true, the root configuration has been possibly invalidated
   */
  private final AtomicBoolean myRootsInvalidated = new AtomicBoolean(true);
  /**
   * If true, there are some configured roots, or listener has never been run yet
   */
  private final AtomicBoolean myHasRoots = new AtomicBoolean(true);
  /**
   * If true, the notification is currently active and has not been dismissed yet.
   */
  private final AtomicBoolean myNotificationPosted = new AtomicBoolean(false);

  private final MergingUpdateQueue myQueue;

  private Notification myNotification;

  /**
   * The invalid roots
   */
  private static final String _INVALID_ROOTS_ID = "";
  /**
   * The command listener
   */
  private final CommandListener myCommandListener;
  /**
   * The file listener
   */
  private final MyFileListener myFileListener;
  /**
   * Listener for refresh events
   */
  private final VirtualFileManagerAdapter myVirtualFileManagerListener;
  /**
   * Local file system service
   */
  private final LocalFileSystem myLocalFileSystem;
  /**
   * The multicaster for root events
   */
  private final RootsListener myMulticaster;

  private final MessageBusConnection myMessageBusConnection;

  /**
   * The constructor
   *
   * @param project     the project instance
   * @param multicaster the listeners to notify
   */
  public RootTracker(Vcs vcs, @NotNull Project project, @NotNull RootsListener multicaster) {

    myMulticaster = multicaster;
    if (project.isDefault()) {
      throw new IllegalArgumentException("The project must not be default");
    }
    myProject = project;
    myProjectRoots = ProjectRootManager.getInstance(myProject);
    myQueue = new MergingUpdateQueue("queue", 500, true, null, project, null, false);
    myVcs = vcs;
    myVcsManager = ProjectLevelVcsManager.getInstance(project);
    myVcsManager.addVcsListener(this);
    myLocalFileSystem = LocalFileSystem.getInstance();
    myMessageBusConnection = myProject.getMessageBus().connect();
    myMessageBusConnection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
        // do nothing
      }

      public void rootsChanged(ModuleRootEvent event) {
        invalidate();
      }
    });
    myCommandListener = new CommandAdapter() {
      @Override
      public void commandFinished(CommandEvent event) {
        if (!myRootsInvalidated.compareAndSet(true, false)) {
          return;
        }
        scheduleRootsCheck(false);
      }
    };
    CommandProcessor.getInstance().addCommandListener(myCommandListener);
    myFileListener = new MyFileListener();
    VirtualFileManagerEx fileManager = (VirtualFileManagerEx)VirtualFileManager.getInstance();
    fileManager.addVirtualFileListener(myFileListener);
    myVirtualFileManagerListener = new VirtualFileManagerAdapter() {
      @Override
      public void afterRefreshFinish(boolean asynchonous) {
        if (!myRootsInvalidated.compareAndSet(true, false)) {
          return;
        }
        scheduleRootsCheck(false);
      }
    };
    fileManager.addVirtualFileManagerListener(myVirtualFileManagerListener);
    StartupManager.getInstance(myProject).runWhenProjectIsInitialized(new Runnable() {
      public void run() {
        myIsEnabled.set(true);
        scheduleRootsCheck(true);
      }
    });
  }

  /**
   * Dispose the component removing all related listeners
   */
  public void dispose() {
    myVcsManager.removeVcsListener(this);
    myMessageBusConnection.disconnect();
    CommandProcessor.getInstance().removeCommandListener(myCommandListener);
    VirtualFileManagerEx fileManager = (VirtualFileManagerEx)VirtualFileManager.getInstance();
    fileManager.removeVirtualFileListener(myFileListener);
    fileManager.removeVirtualFileManagerListener(myVirtualFileManagerListener);
  }

  /**
   * {@inheritDoc}
   */
  public void directoryMappingChanged() {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        scheduleRootsCheck(true);
      }
    });
  }

  private void scheduleRootsCheck(final boolean rootsChanged) {
    if (ApplicationManager.getApplication().isUnitTestMode() || ApplicationManager.getApplication().isHeadlessEnvironment()) {
      doCheckRoots(rootsChanged);
      return;
    }
    myQueue.queue(new Update("root check") {
      public void run() {
        if (myProject.isDisposed()) return;
        doCheckRoots(rootsChanged);
      }
    });
  }

  /**
   * Check roots for changes.
   *
   * @param rootsChanged
   */
  private void doCheckRoots(boolean rootsChanged) {
    if (!myIsEnabled.get() || (!rootsChanged && !myHasRoots.get())) {
      return;
    }

    final HashSet<VirtualFile> rootSet = new HashSet<VirtualFile>();
    boolean hasInvalidRoots = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {
        for (VcsDirectoryMapping m : myVcsManager.getDirectoryMappings()) {
          if (!m.getVcs().equals(myVcs.getName())) {
            continue;
          }
          String path = m.getDirectory();
          if (path.length() == 0) {
            VirtualFile baseDir = myProject.getBaseDir();
            assert baseDir != null;
            path = baseDir.getPath();
          }
          VirtualFile root = lookupFile(path);
          VirtualFile actual = Util.rootOrNull(root);
          if (root == null || rootSet.contains(root) || actual != root) {
            return true;
          }
          rootSet.add(root);
        }
        return false;
      }
    });
    if (!hasInvalidRoots && rootSet.isEmpty()) {
      myHasRoots.set(false);
      return;
    }
    else {
      myHasRoots.set(true);
    }

    if (!hasInvalidRoots) {
      // check if roots have a problem
      for (final VirtualFile root : rootSet) {
        hasInvalidRoots = hasUnmappedSubroots(root, rootSet, 0);
        if (hasInvalidRoots) {
          break;
        }
      }
    }

    if (!hasInvalidRoots) {
      // all roots are correct
      if (myNotificationPosted.compareAndSet(true, false)) {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
          public void run() {
            if (myNotification != null) {
              if (!myNotification.isExpired()) {
                myNotification.expire();
              }

              myNotification = null;
            }
          }
        });
      }
    }
    else if (myNotificationPosted.compareAndSet(false, true)) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        public void run() {
          myNotification = new Notification(_INVALID_ROOTS_ID, Bundle.getString("root.tracker.message.title"),
                                            Bundle.getString("root.tracker.message"), NotificationType.ERROR,
                                            new NotificationListener() {
                                              public void hyperlinkUpdate(@NotNull Notification notification,
                                                                          @NotNull HyperlinkEvent event) {
                                                if (fixRoots()) {
                                                  notification.expire();
                                                }
                                              }
                                            });

          Notifications.Bus.notify(myNotification, myProject);
        }
      });
    }
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      public void run() {
        myMulticaster.RootsChanged();
      }
    });
  }

  /**
   * Check if there are some unmapped subdirectories under
   *
   * @param directory the content root to check
   * @param rootSet   the mapped root set
   * @param depth
   */
  private static boolean hasUnmappedSubroots(final VirtualFile directory, final @NotNull HashSet<VirtualFile> rootSet, int depth) {
    if (depth > 3) { // three is quite enough
      return false;
    }
    VirtualFile[] children = ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile[]>() {
      public VirtualFile[] compute() {
        return directory.isValid() ? directory.getChildren() : VirtualFile.EMPTY_ARRAY;
      }
    });

    for (final VirtualFile child : children) {
      if (!child.isDirectory()) {
        continue;
      }
      if (child.getName().equals(".")) {
        return !rootSet.contains(child.getParent());
      }
      if (hasUnmappedSubroots(child, rootSet, depth + 1)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Fix mapped roots
   *
   * @return true if roots now in the correct state
   */
  boolean fixRoots() {
    final List<VcsDirectoryMapping> vcsDirectoryMappings = new ArrayList<VcsDirectoryMapping>(myVcsManager.getDirectoryMappings());
    final HashSet<String> mapped = new HashSet<String>();
    final HashSet<String> removed = new HashSet<String>();
    final HashSet<String> added = new HashSet<String>();
    final VirtualFile baseDir = myProject.getBaseDir();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        for (Iterator<VcsDirectoryMapping> i = vcsDirectoryMappings.iterator(); i.hasNext();) {
          VcsDirectoryMapping m = i.next();
          String vcsName = myVcs.getName();
          if (!vcsName.equals(m.getVcs())) {
            continue;
          }
          String path = m.getDirectory();
          if (path.length() == 0 && baseDir != null) {
            path = baseDir.getPath();
          }
          VirtualFile file = lookupFile(path);
          if (file != null && !mapped.add(file.getPath())) {
            // eliminate duplicates
            i.remove();
            continue;
          }
          final VirtualFile actual = Util.rootOrNull(file);
          if (file == null || actual == null) {
            removed.add(path);
          }
          else if (actual != file) {
            removed.add(path);
            added.add(actual.getPath());
          }
        }
        for (String m : mapped) {
          VirtualFile file = lookupFile(m);
          if (file == null) {
            continue;
          }
          addSubroots(file, added, mapped);
          if (removed.contains(m)) {
            continue;
          }
          VirtualFile root = Util.rootOrNull(file);
          assert root != null;
          for (String o : mapped) {
            // the mapped collection is not modified here, so order is being kept
            if (o.equals(m) || removed.contains(o)) {
              continue;
            }
            if (o.startsWith(m)) {
              VirtualFile otherFile = lookupFile(m);
              assert otherFile != null;
              VirtualFile otherRoot = Util.rootOrNull(otherFile);
              assert otherRoot != null;
              if (otherRoot == root) {
                removed.add(o);
              }
              else if (otherFile != otherRoot) {
                added.add(otherRoot.getPath());
                removed.add(o);
              }
            }
          }
        }
      }
    });
    if (added.isEmpty() && removed.isEmpty()) {
      Messages.showInfoMessage(myProject, Bundle.message("fix.roots.valid.message"), Bundle.message("fix.roots.valid.title"));
      return true;
    }
    FixRootsDialog d = new FixRootsDialog(myProject, mapped, added, removed);
    d.show();
    if (!d.isOK()) {
      return false;
    }
    for (Iterator<VcsDirectoryMapping> i = vcsDirectoryMappings.iterator(); i.hasNext();) {
      VcsDirectoryMapping m = i.next();
      String path = m.getDirectory();
      if (removed.contains(path) || (path.length() == 0 && baseDir != null && removed.contains(baseDir.getPath()))) {
        i.remove();
      }
    }
    for (String a : added) {
      vcsDirectoryMappings.add(new VcsDirectoryMapping(a, myVcs.getName()));
    }
    myVcsManager.setDirectoryMappings(vcsDirectoryMappings);
    myVcsManager.updateActiveVcss();
    return true;
  }

  /**
   * Look up file in the file system
   *
   * @param path the path to lookup
   * @return the file or null if the file not found
   */
  @Nullable
  private VirtualFile lookupFile(String path) {
    return myLocalFileSystem.findFileByPath(path);
  }

  /**
   * Add subroots for the content root
   *
   * @param directory the content root to check
   * @param toAdd     collection of roots to be added
   * @param mapped    all mapped roots
   */
  private static void addSubroots(VirtualFile directory, HashSet<String> toAdd, HashSet<String> mapped) {
    for (VirtualFile child : directory.getChildren()) {
      if (!child.isDirectory()) {
        continue;
      }
      if (child.getName().equals(".") && !mapped.contains(directory.getPath())) {
        toAdd.add(directory.getPath());
      }
      else {
        addSubroots(child, toAdd, mapped);
      }
    }
  }

  /**
   * Invalidate root
   */
  private void invalidate() {
    myRootsInvalidated.set(true);
  }

  /**
   * The listener for roots
   */
  private class MyFileListener extends VirtualFileAdapter {
    /**
     * Return true if file has repositories
     *
     * @param file the file to check
     * @return true if file has repositories
     */
    private boolean hasRepositories(VirtualFile file) {
      if (!file.isDirectory() || !file.getName().equals(".")) {
        return false;
      }
      VirtualFile baseDir = myProject.getBaseDir();
      if (baseDir == null) {
        return false;
      }
      if (!VfsUtil.isAncestor(baseDir, file, false)) {
        boolean isUnder = false;
        for (VirtualFile c : myProjectRoots.getContentRoots()) {
          if (!VfsUtil.isAncestor(baseDir, c, false) && VfsUtil.isAncestor(c, file, false)) {
            isUnder = true;
            break;
          }
        }
        if (!isUnder) {
          return false;
        }
      }
      return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCreated(VirtualFileEvent event) {
      if (!myHasRoots.get()) {
        return;
      }
      if (hasRepositories(event.getFile())) {
        invalidate();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
      if (!myHasRoots.get()) {
        return;
      }
      if (hasRepositories(event.getFile())) {
        invalidate();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
      if (!myHasRoots.get()) {
        return;
      }
      if (hasRepositories(event.getFile())) {
        invalidate();
      }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
      if (!myHasRoots.get()) {
        return;
      }
      if (hasRepositories(event.getFile())) {
        invalidate();
      }
    }
  }
}
