package org.community.intellij.plugins.communitycase.edit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
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
    //just use the first file's parent as execution dir (other files will be relative to this one)
    VirtualFile execDir=Util.getRoot(virtualFiles[0]).getParent();

    SimpleHandler handler=new SimpleHandler(myProject, execDir, Command.CHECKOUT);
    handler.setSilent(false);
    handler.addParameters("-res");//reserved    //todo wc read from settings whether to reserve or unreserve (–unr)
    handler.addParameters("-nc");//no comment   //todo wc optionally prompt for this
    handler.endOptions();
    for(VirtualFile file:virtualFiles) {
      handler.addParameters(Util.getRelativeFilePath(file, execDir)); //make all other files relative to the exec dir we chose
    }

    handler.run();
  }

  @Override
  public String getRequestText() {
    return "Clear read-only status by checking out file in ClearCase";
  }
}
