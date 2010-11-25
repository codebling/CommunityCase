package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import net.sourceforge.transparent.actions.ActionContext;
import net.sourceforge.transparent.actions.VcsAction;

import java.util.ArrayList;
import java.util.List;

public class CheckInProjectAction extends VcsAction {
   public CheckInProjectAction() {
   }

   protected List execute(ActionContext context) {
      Project project = context.project;
      FileDocumentManager.getInstance().saveAllDocuments();

      CheckInConfig config = CheckInConfig.getInstance(project);
      CheckInProjectDialog dialog = new CheckInProjectDialog(project, config.getCheckInEnvironment());
      dialog.analyzeChanges(false, null);

      ArrayList expections = new ArrayList();
      if (!dialog.hasDiffs()) {
         showUserNothingToCheckInWarning(project, dialog);
      }
      else {
         askUserAndCheckIn(dialog, config, project, expections);
      }
      return expections;
   }

   private void askUserAndCheckIn(CheckInProjectDialog dialog,
                                  CheckInConfig config,
                                  Project project,
                                  ArrayList expections) {
      try {
         if (askForCheckInConfirmation(dialog, config)) {
            AbstractVcsHelper.getInstance(project).doCheckinProject(dialog.getCheckinProjectPanel(),
                                                                    dialog.getComment());
         }
      } catch (VcsException e) {
         expections.add(e);
      }
   }

   private void showUserNothingToCheckInWarning(Project project, CheckInProjectDialog dialog) {
      Messages.showMessageDialog(project, "Nothing to check in", "Nothing Found", Messages.getInformationIcon());
      dialog.dispose();
   }

   private boolean askForCheckInConfirmation(CheckInProjectDialog dialog, CheckInConfig config) throws VcsException {
      config.copyToDialog(dialog);

      dialog.show();

      boolean ok = dialog.isOK();

      if (ok) {
         config.copyFromDialog(dialog);
      }
      return ok;
   }

   protected String getActionName(ActionContext context) {
      return "Check In Project";
   }
}
