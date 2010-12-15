package org.community.intellij.plugins.communitycase.edit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Handler for checking out files.
 *
 * @author worsecodes <worsecodes@gmail.com>
 * @since 12/14/10
 */
public class EditFileProvider implements com.intellij.openapi.vcs.EditFileProvider {
  private final Project myProject;

  public EditFileProvider(@NotNull Project project) {
    myProject=project;
  }

  @Override
  public void editFiles(VirtualFile[] virtualFiles) throws VcsException {
    final VirtualFile vcsRoot = VcsUtil.getVcsRootFor(myProject, virtualFiles[0]);
    final SimpleHandler handler = new SimpleHandler(myProject, vcsRoot, Command.CHECKOUT);
    handler.addParameters("-res");//reserved
    handler.addParameters("-nc");//no comment
    for(VirtualFile f:virtualFiles)
      handler.addParameters(f.getName());
    handler.setSilent(false);

    final String output = handler.run();
  }

  @Override
  public String getRequestText() {
    return "Clear read-only status by checking out file in ClearCase";
  }
}
