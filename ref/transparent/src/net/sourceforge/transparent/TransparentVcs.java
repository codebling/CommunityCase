package net.sourceforge.transparent;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.localVcs.LocalVcsServices;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.wm.WindowManager;
import net.sourceforge.transparent.actions.checkin.CheckInConfig;
import org.intellij.plugins.ExcludedFileFilter;
import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;
import org.intellij.plugins.util.LogUtil;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;

public class TransparentVcs
/*@if Aurora@*/
   extends AbstractVcs implements ProjectComponent,
                                  FileRenameProvider,
                                  FileMoveProvider,
                                  DirectoryRenameProvider,
                                  DirectoryMoveProvider,
                                  TransactionProvider
/*@else@
  implements AbstractVcs, ProjectComponent
  @end@*/ {
   private ClearCase clearcase;
   private TransparentVcsCapabilities vcsCapabilities = new TransparentVcsCapabilities();
   private TransparentConfiguration transparentConfig;
   private CheckInConfig checkInConfiguration;
   private ExcludedPathsFromVcsConfiguration excludedPathsConfiguration;
/*@if Aurora@*/
/*@else@
   private Project                           myProject;
  @end@*/
   private ExcludedFileFilter fileFilter = new ExcludedFileFilter();
   private ExcludedFilesListener excludedFilesListener;
   private TransparentModificationAttemptListener modificationAttemptListener;
   private boolean started;

   public static final Logger LOG = LogUtil.getLogger();
   public static final String TEMPORARY_FILE_SUFFIX = ".deleteAndAdd";

   static public void debug(String s) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(s);
      }
   }

   public TransparentVcs(Project project) {
/*@if Aurora@*/
      super(project);
/*@else@
      myProject = project;
  @end@*/
   }

   public void projectOpened() {
      debug("projectOpened()");
      debug("loading " + getDisplayName() + " for " + getProjectName());
      initTransparentConfiguration();
      initExcludedFilesConfiguration();
      initCheckInConfiguration();
   }

   public void projectClosed() {
   }

   public void initComponent() {

   }

   // TODO To be refactored in plugin utils
   private String getProjectName() {
      String projectName;
      if (isDefaultProject()) {
         projectName = "<default>";
      } else {
         projectName = myProject.getProjectFile().getName();
      }
      return projectName;
   }

   private boolean isDefaultProject() {
      return myProject == null || myProject.getProjectFile() == null;
   }

   public void initTransparentConfiguration() {
      transparentConfig = TransparentConfiguration.getInstance(myProject);
      transparentConfig.addListener(new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent evt) {
            transparentConfigurationChanged();
         }
      });
      transparentConfigurationChanged();
   }

   public void initExcludedFilesConfiguration() {
      excludedPathsConfiguration = ExcludedPathsFromVcsConfiguration.getInstance(myProject);
      excludedPathsConfiguration.addListener(new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent evt) {
            excludedPathsConfigurationChanged();
         }
      });
      excludedPathsConfigurationChanged();
   }

   public void excludedPathsConfigurationChanged() {
      fileFilter = new ExcludedFileFilter(excludedPathsConfiguration.getExcludedPaths(), Collections.EMPTY_LIST);
      debug("reloading excluded file filter to " + fileFilter);
   }

   public void transparentConfigurationChanged() {
      if (!getTransparentConfig().offline) {
         resetClearCaseFromConfiguration();
      }
   }

   private void initCheckInConfiguration() {
      checkInConfiguration = CheckInConfig.getInstance(myProject);
   }


   private void resetClearCaseFromConfiguration() {
      if (clearcase == null || !getTransparentConfig().implementation.equals(clearcase.getName())) {
         try {
            debug("Changing Clearcase interface to " + getTransparentConfig().implementation);
            clearcase = new ClearCaseDecorator(
               (ClearCase) Class.forName(getTransparentConfig().implementation).newInstance());
         } catch (Throwable e) {
            Messages.showMessageDialog(WindowManager.getInstance().suggestParentWindow(getProject()),
                                       e.getMessage() + "\nSelecting CommandLineImplementation instead",
                                       "Error while selecting " +
                                       getTransparentConfig().implementation +
                                       "implementation",
                                       Messages.getErrorIcon());
            getTransparentConfig().implementation = CommandLineClearCase.class.getName();
            clearcase = new ClearCaseDecorator(new CommandLineClearCase());
         }
      }
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
      return "ClearCase";
   }

   public Configurable getConfigurable() {
      return new TransparentConfigurable(myProject);
   }

   public TransparentConfiguration getTransparentConfig() {
      return transparentConfig;
   }

   public AbstractVcsCapabilities getCapabilities() {
      return vcsCapabilities;
   }

   public ClearCase getClearCase() {
      if (clearcase == null) {
         resetClearCaseFromConfiguration();
      }
      return clearcase;
   }

   public void setClearCase(ClearCase clearCase) {
      clearcase = clearCase;
   }

   public Project getProject() {
      return myProject;
   }

   public void start() throws VcsException {
      debug("enter: start()");
      modificationAttemptListener = new TransparentModificationAttemptListener(this);
      modificationAttemptListener.start();
      excludedFilesListener = new ExcludedFilesListener(this);
      excludedFilesListener.start();
      started = true;
   }

   public void shutdown() throws VcsException {
      debug("enter: shutdown()");
      if (modificationAttemptListener != null) {
         modificationAttemptListener.stop();
         modificationAttemptListener = null;
      }
      if (excludedFilesListener != null) {
         excludedFilesListener.stop();
         excludedFilesListener = null;
      }
      started = false;
   }

   public boolean isStarted() { return started; }

   public void startTransaction(Object parameters) throws VcsException {
      debug("enter: startTransaction()");
   }

   public void commitTransaction(Object parameters) throws VcsException {
      debug("enter: commitTransaction()");
   }

   public void rollbackTransaction(Object parameters) {
      debug("enter: rollbackTransaction()");
   }

   public byte[] getFileContent(String path) throws VcsException {
      debug("enter: getFileContent(" + path + ")");
//		throw new UnsupportedOperationException();
      return new byte[0];
   }

   public void checkinFile(String path, Object parameters) throws VcsException {
      debug("enter: checkinFile(" + path + ")");
      if (!fileFilter.accept(path)) {
         return;
      }

      try {
         ClearCaseFile file = getFile(path);
         file.checkIn((String) parameters, isCheckInToUseHijack());
      } catch (Throwable e) {
         handleException(e);
      }
   }

   private boolean isCheckInToUseHijack() {
      return transparentConfig.offline || transparentConfig.checkInUseHijack;
   }

   public boolean checkoutFile(String path, boolean keepHijacked) throws VcsException {
      debug("enter: checkoutFile(" + path + ")");

      if (!fileFilter.accept(path)) {
         return false;
      }

      try {
         ClearCaseFile file = getFile(path);
         file.checkOut(transparentConfig.checkoutReserved, keepHijacked);
         refreshIDEA(file);
         return file.isCheckedOut();
      } catch (Throwable e) {
         handleException(e);
         return false;
      }
   }

   public void undoCheckoutFile(String path) throws VcsException {
      debug("enter: undoCheckoutFile(" + path + ")");
      if (!fileFilter.accept(path)) {
         return;
      }

      try {
         ClearCaseFile file = getFile(path);
         file.undoCheckOut();
         refreshIDEA(file);
      } catch (Throwable e) {
         handleException(e);
      }
   }

   public static VirtualFile getVirtualFile(String path) {
      VirtualFileSystem fileSystem = VirtualFileManager.getInstance().getFileSystem("file");
      VirtualFile virtualFile = fileSystem.findFileByPath(path.replace(File.separatorChar, '/'));
      if (virtualFile == null) {
         LOG.error("could not find file " + path);
      }
      return virtualFile;
   }

   public void refreshIDEA(ClearCaseFile file) {
      VirtualFile virtualFile = getVirtualFile(file.getFile().getPath());
      if (virtualFile != null) {
         virtualFile.refresh(true, false);
      }
   }

   public void addFile(String parentPath, String fileName, Object parameters) throws VcsException {
      debug("enter: addFile(" + parentPath + "," + fileName + ")");

      if (!fileFilter.accept(parentPath + File.separator + fileName)) {
         return;
      }

      try {
         ClearCaseFile file = new ClearCaseFile(new File(parentPath, fileName), getClearCase());
         file.add((String) parameters);
      } catch (Throwable e) {
         handleException(e);
      }
   }

   public void removeFile(String path, final Object parameters) throws VcsException {
      debug("enter: removeFile(" + path + ")");
      if (!fileFilter.accept(path)) {
         return;
      }

      try {
         final File file = new File(path);
         executeAndHandleOtherFileInTheWay(file, new Runnable() {
            public void run() {
               final ClearCaseFile ccFile = getFile(file);
               ccFile.delete((String) parameters);
            }
         });
      } catch (Throwable e) {
         handleException(e);
      }
   }

   public void renameAndCheckInFile(final String path, final String newName, final Object parameters)
      throws VcsException {
      debug("enter: renameAndCheckInFile(" +
            path +
            ",\n" +
            "                            " + newName + ")");

      if (!fileFilter.accept(path)) {
         return;
      }

      try {
         final File oldFile = new File(path);
         final File newFile = new File(oldFile.getParent(), newName);

         executeAndHandleOtherFileInTheWay(oldFile, new Runnable() {
            public void run() {
               renameFile(newFile, oldFile);
               ClearCaseFile oldCCFile = getFile(oldFile);
               if (!oldCCFile.isCheckedIn()) {
                  oldCCFile.checkIn((String) parameters, isCheckInToUseHijack());
               }
               oldCCFile.rename(newName, (String) parameters);
            }
         });
      } catch (Throwable e) {
         handleException(e);
      }
   }

   public void moveRenameAndCheckInFile(String filePath,
                                        String newParentPath,
                                        String newName,
                                        final Object parameters)
      throws VcsException {
      debug("enter: moveRenameAndCheckInFile(" +
            filePath +
            ",\n" +
            "                                " +
            newParentPath +
            ",\n" +
            "                                " + newName + ")");

      if (!fileFilter.accept(filePath)) {
         return;
      }

      try {
         final File oldFile = new File(filePath);
         final File newFile = new File(newParentPath, newName);

         executeAndHandleOtherFileInTheWay(oldFile, new Runnable() {
            public void run() {
               renameFile(newFile, oldFile);
               ClearCaseFile oldCCFile = getFile(oldFile);
               ClearCaseFile newCCFile = getFile(newFile);
               if (!oldCCFile.isCheckedIn()) {
                  oldCCFile.checkIn((String) parameters, isCheckInToUseHijack());
               }
               oldCCFile.move(newCCFile, (String) parameters);
            }
         });
      } catch (Throwable e) {
         handleException(e);
      }
   }

   private void executeAndHandleOtherFileInTheWay(File targetFile, Runnable command) {
      if (isOtherFileInTheWay(targetFile)) {
         executeWithFileInTheWay(targetFile, command);
      } else {
         command.run();
      }
   }

   private boolean isOtherFileInTheWay(File file) {
      return file.exists();
   }

   private void executeWithFileInTheWay(File file, Runnable command) {
      File tmpFile = new File(file.getPath() + TEMPORARY_FILE_SUFFIX);
      renameFile(file, tmpFile);
      try {
         command.run();
      } finally {
         if (!tmpFile.renameTo(file)) {
            throw new ClearCaseException("The file '" +
                                         file.getAbsolutePath() +
                                         "' has been deleted then re-added\n" +
                                         "Check if there is a file '" +
                                         tmpFile.getAbsolutePath() +
                                         "' and rename it back manually");
         }
      }
   }

   private void renameFile(File fileToRename, File newFile) {
      if (!newFile.getParentFile().exists()) {
         if (!newFile.getParentFile().mkdirs()) {
            throw new ClearCaseException("Could not create dir " +
                                         newFile.getParentFile().getAbsolutePath() +
                                         "\n" +
                                         "to move " + fileToRename.getAbsolutePath() + " into");
         }
      }
      if (!fileToRename.renameTo(newFile)) {
         throw new ClearCaseException(
            "Could not move " + fileToRename.getAbsolutePath() + " to " + newFile.getAbsolutePath());
      }
   }

   private void handleException(Throwable e) throws VcsException {
//      Messages.showMessageDialog(_project, e.getMessage(), "Clearcase plugin error", Messages.getErrorIcon());
      debug(e);
      throw new VcsException(e);
   }

   private void debug(Throwable e) {
      LOG.debug(e);
      e.printStackTrace();
   }

   public void addDirectory(String parentPath, String name, Object parameters) throws VcsException {
      debug("enter: addDirectory(" +
            parentPath +
            ",\n" +
            "                    " + name + ")");
      addFile(parentPath, name, parameters);
   }

   public void removeDirectory(String path, Object parameters) throws VcsException {
      removeFile(path, parameters);
   }

   public void renameDirectory(String path, String newName, Object parameters) throws VcsException {
      renameAndCheckInFile(path, newName, parameters);
   }

   public void moveAndRenameDirectory(String path, String newParentPath, String name, Object parameters)
      throws VcsException {
      moveRenameAndCheckInFile(path, newParentPath, name, parameters);      
   }

   public void moveDirectory(String path, String newParentPath, Object parameters) throws VcsException {
      debug("enter: moveDirectory(" +
            path +
            ",\n" +
            "                     " + newParentPath + ")");
      moveRenameAndCheckInFile(path, newParentPath, getFile(path).getName(), parameters);
   }

   public void update() throws VcsException {
      debug("updating");
   }
/*@if Aurora@*/

   public VcsConfiguration getConfiguration() {
      return this.checkInConfiguration;
   }

   public boolean supportsMarkSourcesAsCurrent() {
      return true;
   }

   public boolean markExternalChangesAsUpToDate() {
      return transparentConfig.markExternalChangeAsUpToDate;
   }

   public TransactionProvider getTransactionProvider() {
      return null;
   }

   public FileRenameProvider getFileRenamer() {
      return this;
   }

   public FileMoveProvider getFileMover() {
      return this;
   }

   public DirectoryRenameProvider getDirectoryRenamer() {
      return this;
   }

   public DirectoryMoveProvider getDirectoryMover() {
      return this;
   }

   public UpToDateRevisionProvider getUpToDateRevisionProvider() {
      return LocalVcsServices.getInstance(myProject).getUpToDateRevisionProvider();
   }

   public FileStatusProvider getFileStatusProvider() {
      return LocalVcsServices.getInstance(myProject).getFileStatusProvider();
   }

//   int statusCount = 0;
//   public FileStatusProvider getFileStatusProvider() {return new FileStatusProvider() {
//      public FileStatus getStatus(VirtualFile virtualFile) {
//         FileStatus status = FileStatus.UNKNOWN;
//         switch(++statusCount%7) {
//            case 0:status = FileStatus.ADDED; break;
//            case 1:status = FileStatus.MODIFIED; break;
//            case 2:status = FileStatus.DELETED; break;
//            case 3:status = FileStatus.MERGE; break;
//            case 4:status = FileStatus.NOT_CHANGED; break;
//            case 5:status = FileStatus.OUT_OF_DATE; break;
//            case 6:status = FileStatus.UNKNOWN; break;
//         }
//         return status;
//      }
//   };}
//
   /*@end@*/

   public ExcludedFileFilter getFileFilter() {
      return fileFilter;
   }

   private ClearCaseFile getFile(String path) {
      return new ClearCaseFile(new File(path), getClearCase());
   }

   private ClearCaseFile getFile(File file) {
      return new ClearCaseFile(file, getClearCase());
   }

   public Status getFileStatus(VirtualFile file) {
      return getClearCase().getStatus(new File(file.getPresentableUrl()));
   }



}
