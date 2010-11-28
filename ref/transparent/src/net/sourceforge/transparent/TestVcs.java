package net.sourceforge.transparent;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;

/*@if Aurora@*/
public class TestVcs extends AbstractVcs implements ProjectComponent {
 /*@else@  
public class TestVcs implements AbstractVcs, ProjectComponent {
  @end@*/
    private Project myProject;

    private static final Logger LOG = Logger.getInstance("net.sourceforge.transparent.TransparentVcs");

    public TestVcs(Project project) {
/*@if Aurora@*/
       super(project);
/*@else@
myProject = project;
  @end@*/
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
        System.out.println("loading " + getDisplayName() + " for " + myProject.getProjectFile());
    }

    public void setConfiguration(TransparentConfiguration configuration) {
    }

    public void disposeComponent() {
    }

    public String getComponentName() {
        return getName();
    }

    public String getName() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return "Test";
    }

    public Configurable getConfigurable() {
        return null;
    }

    public AbstractVcsCapabilities getCapabilities() {
        return new AbstractVcsCapabilities() {
            public boolean isDirectoryMovingSupported() {
                return true;
            }

            public boolean isDirectoryRenamingSupported() {
                return true;
            }

            public boolean isFileMovingSupported() {
                return true;
            }

            public boolean isFileRenamingSupported() {
                return true;
            }

            public boolean isTransactionSupported() {
                return true;
            }

        };
    }

    public void start() throws VcsException {
        LOG.debug("enter: start()");
    }

    public void shutdown() throws VcsException {
        LOG.debug("enter: shutdown()");
    }

    public void startTransaction(Object params) throws VcsException {
        LOG.debug("enter: startTransaction()");
    }

    public void commitTransaction(Object params) throws VcsException {
        LOG.debug("enter: commitTransaction()");
    }

    public void rollbackTransaction(Object params) {
        LOG.debug("enter: rollbackTransaction()");
    }

    public byte[] getFileContent(String path) throws VcsException {
        LOG.debug("enter: getFileContent(" + path + ")");
        return new byte[0];
    }

    public void checkinFile(String path,Object params) throws VcsException {
        LOG.debug("enter: checkinFile(" + path + ","+ params + ")");
    }

    public void addFile(String folderPath, String fileName ,Object params) throws VcsException {
        LOG.debug("enter: addFile(" + folderPath + "," + fileName + ")");
    }

    public void removeFile(String path,Object params) throws VcsException {
        LOG.debug("enter: removeFile(" + path + ")");
    }

    public void renameAndCheckInFile(String path, String newName,Object params) throws VcsException {
        LOG.debug("enter: renameAndCheckInFile(" + path + ",\n" +
                  "                            " + newName + ")");
    }

    public void moveRenameAndCheckInFile(String filePath, String newParentPath, String newName,Object params) throws VcsException {
        LOG.debug("enter: moveRenameAndCheckInFile(" + filePath + ",\n" +
                  "                                " + newParentPath + ",\n" +
                  "                                " + newName + ")");
    }


    public void addDirectory(String parentPath, String name,Object params) throws VcsException {
        LOG.debug("enter: addDirectory(" + parentPath + ",\n" +
                "                    " + name + ")");
    }

    public void removeDirectory(String path,Object params) throws VcsException {
        LOG.debug("enter: removeDirectory(" + path + ")");
    }

    public void renameDirectory(String path, String newName,Object params) throws VcsException {
        LOG.debug("enter: renameDirectory(" + path + ",\n" +
                "                       " + newName + ")");
    }

    public void moveDirectory(String path, String newParentPath,Object params) throws VcsException {
        LOG.debug("enter: moveDirectory(" + path + ",\n" +
                "                     " + newParentPath + ")");
    }

}
