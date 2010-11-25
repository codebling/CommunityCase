package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;

public class HistoryAction extends AsynchronousAction {

   public void perform(VirtualFile file, ActionContext context) {
      cleartool(new String[]{"lsh","-g",getVersionExtendedPathName(file, context)});
   }

   protected String getActionName(ActionContext context) {
      return "History";
   }
}
