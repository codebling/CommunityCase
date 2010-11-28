package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.actions.ActionContext;
import net.sourceforge.transparent.actions.SynchronousAction;
import org.intellij.openapi.OpenApiFacade;

public class CheckInFileAction extends SynchronousAction {
   private CheckInOptionsHandler ciHandler = new CheckInOptionsHandler();

   protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
      return OpenApiFacade.getFileStatusManager(context.project).getStatus(file) == FileStatus.MODIFIED;
   }

   protected void perform(VirtualFile file, ActionContext context) throws VcsException {

      if (ciHandler.canCheckIn(file, context)) {
         AbstractVcsHelper.getInstance(context.project).doCheckinFiles(
            new VirtualFile[]{file}, ciHandler.getComment());
      }
   }

   protected void resetTransactionIndicators(ActionContext context) {
       ciHandler.reset();
   }

   protected String getActionName(ActionContext context) {
      return "Check In File";
   }
}
