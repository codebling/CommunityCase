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
package org.community.intellij.plugins.communitycase.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * The application wide settings
 */
@State(
  name = "ClearCase.Application.Settings",
  storages = {@Storage(
    id = "ClearCase.Application.Settings",
    file = "$APP_CONFIG$/vcs.xml")})
class VcsApplicationSettings implements PersistentStateComponent<VcsApplicationSettings.State> {
  /**
   * the default executable
   */
  @NonNls static final String[] DEFAULT_WINDOWS_PATHS =
    {"C:\\Program Files\\ibm\\RationalSDLC\\ClearCase\\bin", "C:\\Program Files (x86)\\ibm\\RationalSDLC\\ClearCase\\bin"};
  /**
   * Windows executable name
   */
  @NonNls static final String DEFAULT_WINDOWS_CLEARTOOL= "cleartool.exe";
  /**
   * Default UNIX paths
   */
  @NonNls static final String[] DEFAULT_UNIX_PATHS = {"/usr/local/bin", "/usr/bin", "/opt/local/bin", "/opt/bin"};
  /**
   * UNIX executable name
   */
  @NonNls static final String DEFAULT_UNIX_CLEARTOOL= "cleartool";
  /**
   * The last used path
   */
  private String myExecutablePath;

  private String myBranchFilter="";
  private String myPathFilter="";

  public static VcsApplicationSettings getInstance() {
    return ServiceManager.getService(VcsApplicationSettings.class);
  }

  /**
   * @return the default executable name depending on the platform
   */
  public String getDefaultPathToExecutable() {
    if (myExecutablePath == null) {
      String[] paths;
      String program;
      if (SystemInfo.isWindows) {
        program =DEFAULT_WINDOWS_CLEARTOOL;
        paths = DEFAULT_WINDOWS_PATHS;
      }
      else {
        program =DEFAULT_UNIX_CLEARTOOL;
        paths = DEFAULT_UNIX_PATHS;
      }
      for (String p : paths) {
        File f = new File(p, program);
        if (f.exists()) {
          myExecutablePath= f.getAbsolutePath();
          break;
        }
      }
      if (myExecutablePath == null) {
        // otherwise, hope it's in $PATH
        myExecutablePath= program;
      }
    }
    return myExecutablePath;
  }

  public State getState() {
    State s = new State();
    s.PATH_TO_CLEARTOOL=myExecutablePath;
    s.BRANCH_FILTER=myBranchFilter;
    s.PATH_FILTER=myPathFilter;
    return s;
  }

  public void loadState(State state) {
    myExecutablePath= state.PATH_TO_CLEARTOOL ==null?getDefaultPathToExecutable():state.PATH_TO_CLEARTOOL;
    myBranchFilter= state.BRANCH_FILTER==null?"":state.BRANCH_FILTER;
    myPathFilter= state.PATH_FILTER==null?"":state.PATH_FILTER;
  }

  /**
   * @return get last set path or null
   */
  public String getPathToExecutable() {
    return myExecutablePath == null ? getDefaultPathToExecutable() :myExecutablePath;
  }

  /**
   * Change last set path to executable
   * @param path the path
   */
  public void setPathToExecutable(String path) {
    myExecutablePath= path;
  }

  public boolean getShowDirectories() {
    return true;
  }

  @NotNull
  public String getBranchFilter() {
    return myBranchFilter==null?"":myBranchFilter;
  }
  public void setBranchFilter(String branchFilter) {
    myBranchFilter=branchFilter;
  }
  public void setPathFilter(String pathFilter) {
    myPathFilter=pathFilter;
  }
  @NotNull
  public String getPathFilter() {
    return myPathFilter==null?"":myPathFilter;
  }

  /**
   * The settings state
   */
  public static class State {
    /**
     * The last saved path
     */
    public String PATH_TO_CLEARTOOL;
    public String BRANCH_FILTER;
    public String PATH_FILTER;
  }
}
