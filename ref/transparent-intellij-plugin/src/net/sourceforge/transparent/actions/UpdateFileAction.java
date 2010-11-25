package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;


public class UpdateFileAction extends AsynchronousAction {
   public static final String HIJACKED_MESSAGE =
      " is hijacked.\nDo you want to continue and lose your local changes?";
   public static final String CHECKED_OUT_MESSAGE =
      " is checked out.\nUndo checkout before updating it";

   public void perform(VirtualFile vfile, ActionContext context) {
      cleartool(new String[]{"update", "-graphical",vfile.getPath()});
   }

   protected String getActionName(ActionContext context) {
      return "Update File";
   }
}
