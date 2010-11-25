package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.openapi.OpenApiFacade;

public class MarkFileAsUpToDateAction extends SynchronousAction {

   protected boolean isEnabled(VirtualFile file, ActionContext context) {
      return true;
   }
   
   protected void perform(VirtualFile file, ActionContext context) throws VcsException {
      context.vcsHelper.markFileAsUpToDate(file);
   }

   protected String getActionName(ActionContext context) {
      return "Mark File as Up-to-date";
   }


}
