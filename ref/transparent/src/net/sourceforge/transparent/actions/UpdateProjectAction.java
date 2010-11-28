package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;

public class UpdateProjectAction extends AsynchronousAction {
   public void perform(VirtualFile file, ActionContext context) {
      cleartool(new String[]{"update","-graphical",context.vcs.getTransparentConfig().clearcaseRoot});
   }

   protected String getActionName(ActionContext context) {
      return "Update Project";
   }
}
