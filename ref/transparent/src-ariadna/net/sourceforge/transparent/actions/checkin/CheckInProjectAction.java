package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.ui.Messages;

import java.util.ArrayList;

import net.sourceforge.transparent.actions.checkin.CheckInHandler;
import net.sourceforge.transparent.actions.VcsAction;
import net.sourceforge.transparent.actions.ActionContext;


public class CheckInProjectAction extends VcsAction {
   static CheckInHandler handler;  // TODO: Have one handler per project => Handler could be a projectcomponent

   public void actionPerformed(AnActionEvent event) {
      ActionContext context = new ActionContext(event);
      logAction("actionPerformed", context);

      context.project.saveAllDocuments();

      CheckinProjectPanel checkinProjectPanel = context.vcsHelper.createCheckinProjectPanel();
      if (checkinProjectPanel.hasDiffs()) {
         doCheckin(context, checkinProjectPanel);
      } else {
         Messages.showMessageDialog(context.project,
                                    "Nothing was found to check in",
                                    "Nothing Found",
                                    Messages.getInformationIcon());
      }
   }

   private void doCheckin(ActionContext context, CheckinProjectPanel checkinProjectPanel) {
      try {
         CheckInProjectDialog checkInProjectDialog = new CheckInProjectDialog(context.project, checkinProjectPanel);

         getHandler(context).setCheckInDialog(checkInProjectDialog);
         if (getHandler(context).askForCheckInConfirmation()) {
            context.vcsHelper.doCheckinProject(checkinProjectPanel, getHandler(context).getComment());
         }
      } catch (VcsException e) {
         handleException(e, context);
      }
   }

   private void handleException(VcsException e, ActionContext context) {
      ArrayList list = new ArrayList();
      list.add(e);
      context.vcsHelper.showErrors(list, getActionName(context));
      e.printStackTrace();
   }

   protected CheckInHandler getHandler(ActionContext context) {
      if (handler == null) {
         handler = new CheckInHandler(context.vcs);
      }
      return handler;
   }

   protected String getActionName(ActionContext context) {
      return "Check In Project to " + context.vcs.getName();
   }

}
