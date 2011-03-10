package org.community.intellij.plugins.communitycase.edit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.LineHandler;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.config.VcsSettings;
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

    //todo wc when converting Commands to Handlers, include file.refresh(false, false) (see VcsHandleType.java:50)
    //  also check for and report errors such as when file is checked out reserved by another user
    LineHandler handler=new LineHandler(myProject, execDir, Command.CHECKOUT);
    handler.setSilent(false);
    handler.setStdoutSuppressed(false);
    VcsSettings settings=VcsSettings.getInstance(myProject);
    if(settings!=null && settings.isUseReservedCheckoutForFiles())
      handler.addParameters("-res");//reserved
    else
      handler.addParameters("–unr");//unreserved
    handler.addParameters("-nc");//no comment   //todo wc optionally prompt for this
    handler.addParameters("-use");//use hijack - force this so that we can clear the read-only flag ahead of command execution
    handler.endOptions();
    for(VirtualFile file:virtualFiles) {
      handler.addParameters(Util.getRelativeFilePath(file, execDir)); //make all other files relative to the exec dir we chose
      new FilePathImpl(file).getIOFile().setWritable(true);
    }

    handler.start();
    for(VirtualFile file:virtualFiles)
      file.refresh(false,false);
  }

  @Override
  public String getRequestText() {
    return "Clear read-only status by checking out file in ClearCase";
  }
}
