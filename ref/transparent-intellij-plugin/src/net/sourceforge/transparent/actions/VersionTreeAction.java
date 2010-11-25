package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;

public class VersionTreeAction extends AsynchronousAction {

   public void perform(VirtualFile file, ActionContext context) {
      cleartool(new String[]{"lsvtree","-g",getVersionExtendedPathName(file, context)});
   }

   protected String getActionName(ActionContext context) {
      return "Version Tree";
   }
}

