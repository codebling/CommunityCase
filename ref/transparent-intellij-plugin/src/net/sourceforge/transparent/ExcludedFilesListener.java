package net.sourceforge.transparent;

import com.intellij.openapi.vfs.*;

import java.io.File;

import org.intellij.plugins.util.CommandUtil;
import org.intellij.openapi.OpenApiFacade;

/**
 * User: sg426575
 * Date: Oct 2, 2003
 * Time: 1:28:37 PM
 */
class ExcludedFilesListener extends VirtualFileAdapter {
   private TransparentVcs vcs;

   public ExcludedFilesListener(TransparentVcs vcs) {
      this.vcs = vcs;
   }

   public void propertyChanged(VirtualFilePropertyEvent event) {
      if (!event.getPropertyName().equals(VirtualFile.PROP_NAME)) {
         return;
      }
      markFilteredFileAsUpToDate(event);
   }

   private void logEvent(VirtualFileEvent event) {
      System.out.println("\n--------------------------------------------");
      if (event == null) {
      System.out.println("Event               =<null>");
         return;
      }
      System.out.println("FileName            ="+event.getFileName());
      System.out.println("File                =" + getValueOrNull(event.getFile()));
      if (event.getFile()!=null) {
      System.out.println("File name           =" + event.getFile().getName());
      System.out.println("File path           =" + event.getFile().getPath());
      System.out.println("File valid          =" + event.getFile().isValid());
      System.out.println("File exists         =" + new File(event.getFile().getPath()).exists());
      }
      System.out.println("Directory           ="+event.isDirectory());
      System.out.println("Parent              ="+getValueOrNull(event.getParent()));
      if (event.getParent()!=null) {
         System.out.println("Parent path         ="+getValueOrNull(event.getParent().getPath()));
      }
      System.out.println("Requestor           ="+getValueOrNull(event.getRequestor()));
      System.out.println("OldModificationStamp="+event.getOldModificationStamp());
      System.out.println("NewModificationStamp="+event.getNewModificationStamp());
   }

   private String getValueOrNull(Object value) {
      return value==null?"<null>":value.toString();
   }

   private void logEvent(VirtualFilePropertyEvent event) {
      logEvent((VirtualFileEvent) event);
      if (event == null) { return; }
      System.out.println("PropertyName        = " + event.getPropertyName());
      System.out.println("OldValue()          = " + event.getOldValue());
      System.out.println("NewValue()          = " + event.getNewValue());
   }

   public void contentsChanged(VirtualFileEvent event) {
      markFilteredFileAsUpToDate(event);
   }

   public void fileCreated(VirtualFileEvent event) {
      markFilteredFileAsUpToDate(event);
   }

   public void fileDeleted(VirtualFileEvent event) {
      logEvent(event);
      markFilteredFileAsUpToDate(event);
   }

   public void fileMoved(VirtualFileMoveEvent event) {
      markFilteredFileAsUpToDate(event);
   }

   private void markFilteredFileAsUpToDate(VirtualFileEvent event) {
      if (event.getFile() == null||event.getParent()==null) {
         return;
      }
      String path = new File(event.getParent().getPath(), event.getFileName()).getPath();
      if (!vcs.getFileFilter().accept(path)) {
         markEventFileAsUpToDate(event);
      }
   }

   private void markEventFileAsUpToDate(final VirtualFileEvent event) {
      try {
         new CommandUtil().runWriteAction(new CommandUtil.Command() {
            public Object run() throws Exception {
               OpenApiFacade.getAbstractVcsHelper(vcs.getProject()).markFileAsUpToDate(event.getFile());
               return null;
            }
         });
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void start() {
      OpenApiFacade.getVirtualFileManager().addVirtualFileListener(this);
   }

   public void stop() {
      OpenApiFacade.getVirtualFileManager().removeVirtualFileListener(this);
   }
}
