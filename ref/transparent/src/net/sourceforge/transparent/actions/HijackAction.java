package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.CheckOutHelper;

public class HijackAction extends SynchronousAction {

   protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
      return !file.isWritable();
   }

   protected void perform(VirtualFile file, ActionContext context) throws VcsException {
      new CheckOutHelper(context.vcs).hijackFile(file);
   }

   protected String getActionName(ActionContext context) {
      return "Hijack File";
   }

}

