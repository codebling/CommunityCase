package org.community.intellij.plugins.communitycase.commands;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;


public class ClearExplorerHandler {

  private LineHandler ceHandler;

  /**
   * A constructor
   *
   * @param project   a project
   * @param directory a process directory
   */
  public ClearExplorerHandler(@NotNull Project project,@NotNull File directory) {
    ceHandler=new LineHandler(project,directory,Command.VERSION); //create Handler with arbitrary command
    ceHandler.myCommandLine=new GeneralCommandLine();
    String binPath=null;
    if(ceHandler.mySettings!=null)
      binPath=ceHandler.mySettings.getPathToExecutable();
    binPath=new File(binPath).getParent() + File.separator + "clearexplorer.exe";
    ceHandler.myCommandLine.setExePath(binPath);
    ceHandler.myCommandLine.setWorkingDirectory(directory);
    ceHandler.addParameters(directory.toString());
  }

  public boolean isValid() {
    File f=new File(ceHandler.myCommandLine.getExePath());
    return f.exists();
  }

  public void start() {
    ceHandler.start();
  }
}
