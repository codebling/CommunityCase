package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;


public class PropertiesAction extends AsynchronousAction {

   public void perform(VirtualFile file, ActionContext context) {
      cleartool(new String[]{"describe","-g",file.getPath()});
   }

   protected String getActionName(ActionContext context) {
      return "Properties";
   }
}

