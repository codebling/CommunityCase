package net.sourceforge.transparent.actions;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.TransparentVcs;


public class FindCheckoutsAction extends AsynchronousAction {
   static boolean isReminderShown = false;

   public void perform(VirtualFile file, ActionContext context) {
      if (!isReminderShown) {
         isReminderShown = true;
         Messages.showMessageDialog(context.vcs.getProject(),
                                    "Do not check in files from outside IDEA.\nUse Checkin In Project (Ctrl-K) instead",
                                    "Clearcase plugin warning",
                                    Messages.getWarningIcon());
      }
      cleartool(new String[]{"lscheckout","-g",getTargetPath(context.vcs, file.getPath())});
   }

   protected String getActionName(ActionContext context) {
      return "Find Checkouts";
   }

   protected String getTargetPath(TransparentVcs vcs, String filePath) {
      return filePath;
   }
}

