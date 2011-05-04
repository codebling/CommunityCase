package org.community.intellij.plugins.communitycase.actions;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.edit.EditFileProvider;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Checkout extends BasicAction {

  private static final Logger log=Logger.getInstance("#"+Checkout.class.getName());
  private static final String NAME=Bundle.getString("checkout.action.name");

  public Checkout() {
    super(NAME);
  }

  /**
   * Perform the action over set of files
   *
   * @param project       the context project
   * @param mksVcs        the vcs instance
   * @param exceptions    the list of exceptions to be collected.
   * @param files the files to be affected by the operation
   * @return true if the operation scheduled a background job, or cleanup is not needed
   */
  @Override
  protected boolean perform(@NotNull Project project,
                            Vcs mksVcs,
                            @NotNull List<VcsException> exceptions,
                            @NotNull VirtualFile[] files) {
    try {
      new EditFileProvider(project).editFiles(checkIfEnabled(project,mksVcs,files).toArray(new VirtualFile[0]));
    } catch(VcsException e) {
      //log.error(Bundle.getString("checkout.action.error"),e);
      exceptions.add(e);
      return false;
    }

    return true;
  }

  /**
   * @return the name of action (it is used in a number of ui elements)
   */
  @NotNull
  @Override
  protected String getActionName() {
    return NAME;
  }

  /**
   * Check if the action should be enabled for the set of the fils
   *
   * @param project the context project
   * @param vcs     the vcs to use
   * @param vFiles  the set of files
   * @return true if the action should be enabled
   */
  @Override
  protected boolean isEnabled(@NotNull Project project, @NotNull Vcs vcs, @NotNull VirtualFile... vFiles) {
    return checkIfEnabled(project,vcs,vFiles).size() >= 1;
  }

  private Collection<VirtualFile> checkIfEnabled(@NotNull Project project,
                                                 @NotNull Vcs vcs,
                                                 @NotNull VirtualFile... vFiles) {
    Collection<VirtualFile> enabled=new ArrayList<VirtualFile>();
    for(VirtualFile file:vFiles) {
      FileStatus fileStatus=FileStatusManager.getInstance(project).getStatus(file);
      if(fileStatus!=FileStatus.IGNORED
          && fileStatus!=FileStatus.UNKNOWN
          && fileStatus!=FileStatus.MODIFIED)
        enabled.add(file);
    }
    return enabled;
  }
}