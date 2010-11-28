/*
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: Sep 10, 2002
 * Time: 2:49:39 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package net.sourceforge.transparent.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.TransactionRunnable;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.plugins.util.CommandUtil;

import javax.swing.*;
import java.util.List;

//ss

public abstract class SynchronousAction extends FileAction {
   protected boolean isCancelled;
   private CommandUtil commandUtil;

   public SynchronousAction(CommandUtil commandUtil) {
      this.commandUtil = commandUtil;
   }
   public SynchronousAction() {
      this(new CommandUtil());
   }

   protected List runAction(ActionContext context) {
      context.isActionRecursive = isActionRecursive(context);
      isCancelled = false;
      return super.runAction(context);
   }

   protected boolean isActionRecursive(ActionContext context) {
      for (int i = 0; i < context.files.length; i++) {
         VirtualFile file = context.files[i];
         if (file.isDirectory()) {
            return askIfShouldRecurse(context);
         }
      }
      return false;
   }

   protected boolean askIfShouldRecurse(ActionContext context) {
      int r = Messages.showYesNoDialog(context.vcs.getProject(),
                                       "Should the action be recursive",
                                       "Recursive Action Question",
                                       Messages.getQuestionIcon());

      return r == JOptionPane.YES_OPTION;
   }

   protected List execute(final ActionContext context) {
      List exceptions = context.vcsHelper.runTransactionRunnable(context.vcs, new TransactionRunnable() {
         public void run(List exceptions) {
            execute(exceptions, context);
         };
      }, "");

      return exceptions;
   }

   protected void execute(List exceptions, final ActionContext context) {
      for (int i = 0; i < context.files.length; i++) {
         VirtualFile file = context.files[i];
         execute(file, context, exceptions);
      }
      resetTransactionIndicators(context);
   }

   private void execute(final VirtualFile file, final ActionContext context, List exceptions) {
      if (isCancelled) {return;}
      try {
         performAndRefreshStatus(file, context);
      } catch (VcsException ex) {
         ex.setVirtualFile(file);
         exceptions.add(ex);
      } catch (RuntimeException ex) {
         VcsException e = new VcsException(ex);
         e.setVirtualFile(file);
         exceptions.add(e);
      }
      handleRecursiveExecute(file, context, exceptions);
   }

   private void handleRecursiveExecute(final VirtualFile file, final ActionContext context, List exceptions) {
      if (isCancelled) {return;}
      if (file.isDirectory() && context.isActionRecursive) {
         final VirtualFile[] children = file.getChildren();
         for (int i = 0; i < children.length; i++) {
            VirtualFile child = children[i];
            execute(child, context, exceptions);
         }
      }
   }

   private void performAndRefreshStatus(final VirtualFile file, final ActionContext context) throws VcsException {
      if (!isEnabled(file, context)) {
         return;
      }
      perform(file, context);
      commandUtil.runWriteActionWithoutException(new CommandUtil.Command() {
         public Object run() {
            file.refresh(false, true, new Runnable() {
               public void run() {
                  refreshIDEAFileStatus(context.project, file);
               }
            }); return null;
         }
      });
   }

   private void refreshIDEAFileStatus(Project project, final VirtualFile file) {
/*@if Aurora@*/
      AbstractVcsHelper.getInstance(project).markFileAsUpToDate(file);
      OpenApiFacade.getFileStatusManager(project).fileStatusChanged(file);
      /*@else@
           OpenApiFacade.getFileStatusManager(project).refreshFileStatus(file);
       @end@*/
   }


   protected abstract void perform(VirtualFile file, ActionContext context) throws VcsException;

   protected void resetTransactionIndicators(ActionContext context) {
   }

}
