package net.sourceforge.transparent.actions;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.Status;

import javax.swing.*;

import org.intellij.plugins.util.CommandUtil;

public class CheckOutAction extends SynchronousAction {

   public CheckOutAction() { }
   public CheckOutAction(CommandUtil commandUtil) {
      super(commandUtil);
   }


   protected void perform(VirtualFile file, ActionContext context) throws VcsException {
       boolean keepHijack = false;
       Status fileStatus = context.vcs.getFileStatus(file);
       if (fileStatus == Status.HIJACKED)
       {
           String message = "The file "+ file.getPresentableUrl() +" has been hijacked. \nWould you like to use it as the checked-out file?\n  If not it will be lost.";
           int answer = Messages.showYesNoDialog(context.vcs.getProject(),
                                                 message,
                                                 "Checkout hijacked file",
                                                 Messages.getQuestionIcon());
           keepHijack = (answer == JOptionPane.OK_OPTION);
       }
      else if (fileStatus == Status.NOT_AN_ELEMENT) {
         throw new VcsException("Check Out : File is not an element.");
      } else if (fileStatus == Status.CHECKED_OUT) {
         return;
      }

      context.vcs.checkoutFile(file.getPath(), keepHijack);
   }

   protected String getActionName(ActionContext context) {
      return "Checking Out File";
   }

}

