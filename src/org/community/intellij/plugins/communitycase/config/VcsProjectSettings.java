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
package org.community.intellij.plugins.communitycase.config;

import com.intellij.lifecycle.PeriodicalTasksCloser;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.community.intellij.plugins.communitycase.config.VcsSettings.UpdateChangesPolicy;
import static org.community.intellij.plugins.communitycase.config.VcsSettings.UpdateType;
import static org.community.intellij.plugins.communitycase.config.VcsSettings.ConversionPolicy;

/**
 * VCS settings
 */
@State(
  name = "ClearCase.Settings",
  storages = {@Storage(
    id = "ws",
    file = "$WORKSPACE_FILE$")})
class VcsProjectSettings implements PersistentStateComponent<VcsProjectSettings.State> {
  private boolean myCheckoutIncludesTags = false;
  private UpdateChangesPolicy myUpdateChangesPolicy = UpdateChangesPolicy.STASH; // The policy that specifies how files are saved before update or rebase
  private UpdateType myUpdateType = UpdateType.BRANCH_DEFAULT; // The type of update operation to perform
  private ConversionPolicy myLineSeparatorsConversion = ConversionPolicy.PROJECT_LINE_SEPARATORS; // The crlf conversion policy
  private boolean myAskBeforeLineSeparatorConversion = true; // If true, the dialog is shown with conversion options
  private UpdateChangesPolicy myPushActiveBranchesRebaseSavePolicy = UpdateChangesPolicy.STASH; // The policy used in push active branches dialog

  private Boolean myIsBranchFilterAppwide=true;
  private Boolean myIsPathFilterAppwide=true;
  private String myBranchFilter="";
  private String myPathFilter="";
  private boolean myPreserveKeepFiles=false;
  private boolean myReserveFiles=true;
  private boolean myReserveDirectories=false;

  /**
   * @return save policy for push active branches dialog
   */
  public UpdateChangesPolicy getPushActiveBranchesRebaseSavePolicy() {
    return myPushActiveBranchesRebaseSavePolicy;
  }

  /**
   * Change save policy for push active branches dialog
   *
   * @param pushActiveBranchesRebaseSavePolicy
   *         the new policy value
   */
  public void setPushActiveBranchesRebaseSavePolicy(UpdateChangesPolicy pushActiveBranchesRebaseSavePolicy) {
    myPushActiveBranchesRebaseSavePolicy = pushActiveBranchesRebaseSavePolicy;
  }

  /**
   * @return true if before converting line separators user is asked
   */
  public boolean getAskBeforeLineSeparatorConversion() {
    return myAskBeforeLineSeparatorConversion;
  }

  /**
   * Modify user notification policy about line separators
   *
   * @param askBeforeLineSeparatorConversion
   *         a new policy value
   */
  public void setAskBeforeLineSeparatorConversion(boolean askBeforeLineSeparatorConversion) {
    myAskBeforeLineSeparatorConversion = askBeforeLineSeparatorConversion;
  }

  /**
   * @return policy for converting line separators
   */
  public ConversionPolicy getLineSeparatorsConversion() {
    return myLineSeparatorsConversion;
  }

  /**
   * Modify line separators policy
   *
   * @param lineSeparatorsConversion the new policy value
   */
  public void setLineSeparatorsConversion(ConversionPolicy lineSeparatorsConversion) {
    myLineSeparatorsConversion = lineSeparatorsConversion;
  }

  /**
   * @return update type
   */
  public UpdateType getUpdateType() {
    return myUpdateType;
  }

  /**
   * Set update type
   *
   * @param updateType the update type to set
   */
  public void setUpdateType(UpdateType updateType) {
    myUpdateType = updateType;
  }

  /**
   * @return get (a possibly converted value) of update stash policy
   */
  @NotNull
  public UpdateChangesPolicy updateChangesPolicy() {
    return myUpdateChangesPolicy;
  }

  /**
   * Save update changes policy
   *
   * @param value the value to save
   */
  public void setUpdateChangesPolicy(UpdateChangesPolicy value) {
    myUpdateChangesPolicy = value;
  }

  /**
   * {@inheritDoc}
   */
  public State getState() {
    State s = new State();
    s.CHECKOUT_INCLUDE_TAGS = myCheckoutIncludesTags;
    s.LINE_SEPARATORS_CONVERSION = myLineSeparatorsConversion;
    s.LINE_SEPARATORS_CONVERSION_ASK = myAskBeforeLineSeparatorConversion;
    s.PUSH_ACTIVE_BRANCHES_REBASE_SAVE_POLICY = myPushActiveBranchesRebaseSavePolicy;
    s.UPDATE_CHANGES_POLICY = myUpdateChangesPolicy;
    s.UPDATE_STASH = true;
    s.UPDATE_TYPE = myUpdateType;

    s.IS_BRANCH_FILTER_APPWIDE=myIsBranchFilterAppwide;
    s.IS_PATH_FILTER_APPWIDE=myIsPathFilterAppwide;
    s.BRANCH_FILTER=myBranchFilter;
    s.PATH_FILTER=myPathFilter;
    s.PRESERVE_KEEP_FILES=myPreserveKeepFiles;
    return s;
  }

  /**
   * {@inheritDoc}
   */
  public void loadState(State s) {
    myCheckoutIncludesTags = s.CHECKOUT_INCLUDE_TAGS == null ? false : s.CHECKOUT_INCLUDE_TAGS;
    myLineSeparatorsConversion = s.LINE_SEPARATORS_CONVERSION;
    myAskBeforeLineSeparatorConversion = s.LINE_SEPARATORS_CONVERSION_ASK;
    myPushActiveBranchesRebaseSavePolicy = s.PUSH_ACTIVE_BRANCHES_REBASE_SAVE_POLICY;
    myUpdateChangesPolicy = s.UPDATE_CHANGES_POLICY;
    if (myUpdateChangesPolicy == null) {
      myUpdateChangesPolicy = s.UPDATE_STASH ? UpdateChangesPolicy.STASH : UpdateChangesPolicy.KEEP;
    }
    myUpdateType = s.UPDATE_TYPE;

    myIsBranchFilterAppwide=s.IS_BRANCH_FILTER_APPWIDE;
    myIsPathFilterAppwide=s.IS_PATH_FILTER_APPWIDE;
    myBranchFilter=s.BRANCH_FILTER;
    myPathFilter=s.PATH_FILTER;
    myPreserveKeepFiles=s.PRESERVE_KEEP_FILES;
  }

  /**
   * Get git setting for the project
   *
   * @param project a context project
   * @return the git settings
   */
  @Nullable
  public static VcsProjectSettings getInstance(Project project) {
    if (project == null || project.isDisposed()) {
      return null;
    }
    return PeriodicalTasksCloser.getInstance().safeGetService(project, VcsProjectSettings.class);
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

  public Boolean isBranchFilterAppwide() {
    return myIsBranchFilterAppwide;
  }
  public Boolean isPathFilterAppwide() {
    return myIsPathFilterAppwide;
  }
  public void setBranchFilterAppwide(boolean isBranchFilterAppwide) {
    myIsBranchFilterAppwide=isBranchFilterAppwide;
  }
  public void setPathFilterAppwide(boolean isPathFilterAppwide) {
    myIsPathFilterAppwide=isPathFilterAppwide;
  }

  public boolean isPreserveKeepFiles() {
    return myPreserveKeepFiles;
  }

  public void setPreserveKeepFiles(boolean preserveKeepFiles) {
    myPreserveKeepFiles=preserveKeepFiles;
  }

  public boolean isUseReservedCheckoutForFiles() {
    return myReserveFiles;
  }

  public void setUseReservedCheckoutForFiles(boolean useReserved) {
    myReserveFiles=useReserved;
  }

  public boolean isUseReservedCheckoutForDirectories() {
    return myReserveDirectories;
  }

  public void setUseReservedCheckoutForDirectories(boolean useReserved) {
    myReserveDirectories=useReserved;
  }

  /**
   * The state fo the settings
   */
  public static class State {

    /**
     * Checkout includes tags
     */
    public Boolean CHECKOUT_INCLUDE_TAGS;
    /**
     * True if stash/unstash operation should be performed before update (Obsolete option)
     */
    public boolean UPDATE_STASH = true;
    /**
     * The policy that specifies how files are saved before update or rebase
     */
    public UpdateChangesPolicy UPDATE_CHANGES_POLICY = null;
    /**
     * The type of update operation to perform
     */
    public UpdateType UPDATE_TYPE = UpdateType.BRANCH_DEFAULT;
    /**
     * The crlf conversion policy
     */
    public ConversionPolicy LINE_SEPARATORS_CONVERSION = ConversionPolicy.PROJECT_LINE_SEPARATORS;
    /**
     * If true, the dialog is shown with conversion options
     */
    public boolean LINE_SEPARATORS_CONVERSION_ASK = true;
    /**
     * The policy used in push active branches dialog
     */
    public UpdateChangesPolicy PUSH_ACTIVE_BRANCHES_REBASE_SAVE_POLICY = UpdateChangesPolicy.STASH;

    public Boolean IS_BRANCH_FILTER_APPWIDE;
    public Boolean IS_PATH_FILTER_APPWIDE;
    public String BRANCH_FILTER;
    public String PATH_FILTER;
    public boolean PRESERVE_KEEP_FILES;
  }

}
