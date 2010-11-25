package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.openapi.OpenApiFacade;

public class UndoCheckOutAction extends SynchronousAction {
    protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
       return OpenApiFacade.getFileStatusManager(context.project).getStatus(file) == FileStatus.MODIFIED;
    }

//        if (!(context.vcs.getClearCase().getStatus(new File(file.getPresentableUrl())) == Status.CHECKED_OUT))
//            throw new VcsException("Undo Check Out : File is not checked out.");
//        return true;
//    }

//   public void update(AnActionEvent e) {
//      super.update(e);
//      ActionContext context = new ActionContext(e);
//      if (hasFileTarget(context)) {
//         Status fileStatus = context.vcs.getFileStatus(context.files[0]);
//            Presentation presentation = e.getPresentation();
//         if (fileStatus == Status.HIJACKED) {
//            presentation.setText("Undo Hijack");
//         } else {
//            presentation.setText("Undo Check Out");
//         }
//      }
//   }

   protected void perform(VirtualFile file, ActionContext context) throws VcsException {
      context.vcs.undoCheckoutFile(file.getPath());
   }
   protected String getActionName(ActionContext context) {
      return "Undo Check Out File";
   }

}

