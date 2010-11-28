package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.actions.checkin.CheckInOptionsHandler;
import net.sourceforge.transparent.TransparentVcs;

import java.io.File;

public class AddAction extends SynchronousAction {
   private CheckInOptionsHandler ciHandler = new CheckInOptionsHandler();

   protected boolean isEnabled(VirtualFile file, ActionContext context) {
      return true;
   }

   protected void perform(VirtualFile file, ActionContext context) throws VcsException {
      if (ciHandler.canCheckIn(file, context)) {
         doAdd(context.vcs,
               file.getParent().getPath().replace('/', File.separatorChar),
               file.getName(),
               file.isDirectory(),
               ciHandler.getComment());
      }
   }

   private void doAdd(TransparentVcs vcs, String parentPath, String fileName, boolean isDirectory, String comment) throws VcsException {
      if (isDirectory) {
         vcs.addDirectory(parentPath, fileName, comment);
      } else {
         vcs.addFile(parentPath, fileName, comment);
      }
   }

   protected void resetTransactionIndicators(ActionContext context) {
      ciHandler.reset();
   }

   protected String getActionName(ActionContext context) {
      return "Add File";
   }

}
