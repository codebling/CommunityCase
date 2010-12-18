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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.containers.HashMap;
import com.intellij.util.containers.HashSet;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.config.ConfigUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The tracker for ".ignore" and "./info/exclude" files. If it detects changes
 * in ignored files in the project it dirties the project state. The following changes
 * are detected:
 * <ul>
 * <li>Addition or removal of the "./info/exclude" or ".ignore" files</li>
 * <li>The content change of ignore configuration (using modification date change)</li>
 * <li>VCS root change (the roots are rescanned, but files are not marked dirty)</li>
 * </ul>
 * The entire subdirectory is dirtied. The scanner assumes that the repositories
 * are correctly configured. In the case of incorrect configuration some events could be
 * missed.
 */
public class IgnoreTracker {
  private static final Logger log = Logger.getInstance(IgnoreTracker.class.getName());

  /**
   * The vcs manager that tracks content roots
   */
  private final ProjectLevelVcsManager myVcsManager;
  /**
   * The context project
   */
  private final Project myProject;
  /**
   * The vcs instance
   */
  private final Vcs myVcs;
  /**
   * The local exclude path
   */
  private static final String LOCAL_EXCLUDE = "./info/exclude";
  /**
   * The local exclude path
   */
  private static final String[] LOCAL_EXCLUDE_ARRAY = LOCAL_EXCLUDE.split("/");
  /**
   * The folder
   */
  private static final String _FOLDER = ".";
  /**
   * Dirty scope manager
   */
  private final VcsDirtyScopeManager myDirtyScopeManager;
  /**
   * The listener for vcs events
   */
  private final RootsListener myVcsListener;
  /**
   * The listener for file events
   */
  private final MyFileListener myFileListener;
  /**
   * The configuration listener
   */
  private final ConfigListener myConfigListener;
  /**
   * The map from roots to paths of exclude files from config
   */
  private final Map<VirtualFile, String> myExcludeFiles = new HashMap<VirtualFile, String>();
  /**
   * The map from roots to paths of exclude files from config
   */
  private final Set<String> myExcludeFilesPaths = new HashSet<String>();
  /**
   * Cygwin absolute path prefix
   */
  private static final String CYGDRIVE_PREFIX = "/cygdrive/";

  /**
   * The constructor for service
   *
   * @param project the context project
   * @param vcs     the vcs instance
   */
  public IgnoreTracker(Project project, Vcs vcs) {
    myProject = project;
    myVcs = vcs;
    myVcsManager = ProjectLevelVcsManager.getInstance(project);
    myDirtyScopeManager = VcsDirtyScopeManager.getInstance(project);
    myVcsListener = new RootsListener() {
      public void RootsChanged() {
        scan();
      }
    };
    myConfigListener = new ConfigListener() {
      public void configChanged(@NotNull VirtualFile Root, @Nullable VirtualFile configFile) {
        String oldPath;
        synchronized (myExcludeFiles) {
          if (!myExcludeFiles.containsKey(Root)) {
            return;
          }
          oldPath = myExcludeFiles.get(Root);
        }
        String newPath = getExcludeFile(Root);
        if (oldPath == null ? newPath == null : oldPath.equals(newPath)) {
          return;
        }
        synchronized (myExcludeFiles) {
          myExcludeFiles.put(Root, newPath);
          myExcludeFilesPaths.clear();
          myExcludeFilesPaths.addAll(myExcludeFiles.values());
        }
        myDirtyScopeManager.dirDirtyRecursively(Root);
      }
    };
    myVcs.addRootsListener(myVcsListener);
    myVcs.addConfigListener(myConfigListener);
    myFileListener = new MyFileListener();
    VirtualFileManager.getInstance().addVirtualFileListener(myFileListener);
    scan();
  }

  /**
   * This method is invoked when component is started or when vcs root mapping changes.
   */
  public void scan() {
    VirtualFile[] contentRoots = myVcsManager.getRootsUnderVcs(myVcs);
    if (contentRoots == null || contentRoots.length == 0) {
      return;
    }
    HashMap<VirtualFile, String> newRoots = new HashMap<VirtualFile, String>();
    for (VirtualFile r : contentRoots) {
      VirtualFile root = scanParents(r);
      if (!newRoots.containsKey(root)) {
        newRoots.put(root, getExcludeFile(root));
      }
      // note that the component relies on root tracker to scan all children including .ignore files.
    }
    synchronized (myExcludeFiles) {
      myExcludeFiles.clear();
      myExcludeFiles.putAll(newRoots);
      myExcludeFilesPaths.clear();
      myExcludeFilesPaths.addAll(myExcludeFiles.values());
    }
  }

  /**
   * Get normalized path for root and visit config path so it will be noticed by file events
   *
   * @param root the root to examine
   * @return the normalized path
   */
  @Nullable
  private String getExcludeFile(VirtualFile root) {
    try {
      String file = ConfigUtil.getValue(myProject, root, "core.excludesfile");
      file = fixFileName(file);
      if (file != null && file.trim().length() != 0) {
        // locate path so it will be tracked
        VirtualFile fileForPath = LocalFileSystem.getInstance().findFileByPath(file);
        if (fileForPath != null) {
          return fileForPath.getPath();
        }
      }
    }
    catch (VcsException e) {
      log.error(e); //just log and return null
    }
    return null;
  }

  /**
   * Fix name for the file
   *
   * @param file the file name to fix
   * @return the file name is fixed according to MSYS and cygwin rules
   */
  private String fixFileName(String file) {
    if (SystemInfo.isWindows && file != null && file.startsWith("/")) {
      int cp = CYGDRIVE_PREFIX.length();
      if (file.startsWith(CYGDRIVE_PREFIX) && file.length() > cp + 3 && Character.isLetter(file.charAt(cp)) && file.charAt(cp + 1) == '/') {
        // cygwin absolute path syntax is used
        return String.valueOf(file.charAt(cp)) + ":" + file.substring(cp + 1);
      }
      if (file.length() > 3 && Character.isLetter(file.charAt(1)) && file.charAt(2) == '/') {
        // msys drive syntax is used
        return String.valueOf(file.charAt(1)) + ":" + file.substring(2);
      }
      // otherwise the path is relative to "bin/.exe"
      File Dir = new File(myVcs.getAppSettings().getPathToVcs()).getParentFile();
      if (Dir != null) {
        Dir = Dir.getParentFile();
      }
      if (Dir != null) {
        return new File(Dir, file.substring(1)).getPath();
      }
    }
    return file;
  }

  /**
   * Scan this root and parents in the search of .ignore
   *
   * @param root the directory to scan
   * @return VirtualFile ?
   */
  @Nullable
  private static VirtualFile scanParents(VirtualFile root) {
    VirtualFile meta = root.findChild(_FOLDER);
    if (meta != null) {
      final VirtualFile localExclude = root.findFileByRelativePath(LOCAL_EXCLUDE);
      if (localExclude != null && localExclude.isValid()) {
        localExclude.getTimeStamp();
      }
      return root;
    }
    else {
      VirtualFile parent = root.getParent();
      if (parent != null) {
        return scanParents(parent);
      }
    }
    return null;
  }

  /**
   * Dispose the component removing all related listeners
   */
  public void dispose() {
    myVcs.removeRootsListener(myVcsListener);
    myVcs.removeConfigListener(myConfigListener);
    VirtualFileManager.getInstance().removeVirtualFileListener(myFileListener);
  }

  /**
   * The file listener
   */
  class MyFileListener extends VirtualFileAdapter {
    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCreated(VirtualFileEvent event) {
      checkIgnoreConfigChange(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
      checkIgnoreConfigChange(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event) {
      if (".ignore".equals(event.getFileName())) {
        myDirtyScopeManager.dirDirtyRecursively(event.getNewParent());
        myDirtyScopeManager.dirDirtyRecursively(event.getOldParent());
      }
      checkExcludeFile(event.getOldParent().findChild(event.getFileName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileMoved(VirtualFileMoveEvent event) {
      checkExcludeFile(event.getNewParent().findChild(event.getFileName()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fileCopied(VirtualFileCopyEvent event) {
      checkIgnoreConfigChange(event.getFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void contentsChanged(VirtualFileEvent event) {
      checkIgnoreConfigChange(event.getFile());
    }

    /**
     * Check if the event affects dirty scope configuration, and if this the case, notify dirty scope manager.
     *
     * @param file the file to check
     */
    private void checkIgnoreConfigChange(VirtualFile file) {
      if (".ignore".equals(file.getName())) {
        VirtualFile parent = file.getParent();
        if (parent != null) {
          myDirtyScopeManager.dirDirtyRecursively(parent);
        }
        return;
      }
      final VirtualFile base = Util.getPossibleBase(file, LOCAL_EXCLUDE_ARRAY);
      if (base != null) {
        myDirtyScopeManager.dirDirtyRecursively(base);
        return;
      }
      checkExcludeFile(file);
    }

    /**
     * Check if the file is exclude file (specified in config) and process it
     *
     * @param file the file to process
     */
    private void checkExcludeFile(VirtualFile file) {
      String path = file.getPath();
      List<VirtualFile> toDirty = null;
      synchronized (myExcludeFiles) {
        if (myExcludeFilesPaths.contains(path)) {
          toDirty = new ArrayList<VirtualFile>();
          for (Map.Entry<VirtualFile, String> entry : myExcludeFiles.entrySet()) {
            if (path.equals(entry.getValue())) {
              toDirty.add(entry.getKey());
            }
          }
        }
      }
      if (toDirty != null) {
        for (VirtualFile f : toDirty) {
          myDirtyScopeManager.dirDirtyRecursively(f);
        }
      }
    }
  }
}
