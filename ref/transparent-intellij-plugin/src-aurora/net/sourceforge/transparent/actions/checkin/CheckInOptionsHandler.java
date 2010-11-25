package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.actions.ActionContext;

public class CheckInOptionsHandler {
   private boolean forAllChosen = false;
   private String comment;

   public boolean canCheckIn(VirtualFile file, ActionContext context) throws VcsException {
      CheckInConfig config = CheckInConfig.getInstance(context.project);
      CheckInFileDialog d = new CheckInFileDialog(context.project,
                                                  new VirtualFile[]{file},
                                                  config.getCheckInEnvironment(),
                                                  context.event.getInputEvent().getModifiers()) {
      };
      return askForCheckInConfirmation(d, config);
   }

   public boolean askForCheckInConfirmation(CheckInFileDialog dialog, CheckInConfig config) throws VcsException {
      if (forAllChosen) {
         return true;
      }

      config.copyToDialog(dialog);
      dialog.show();
      int dialogResult = dialog.getExitCode();
      if (dialogResult == CheckInFileDialog.CANCEL_EXIT_CODE) {
         return false;
      }
      if (dialogResult == CheckInFileDialog.OK_ALL_EXIT_CODE) {
         forAllChosen = true;
      }

      config.copyFromDialog(dialog);
      comment = dialog.getComment();
      return true;
   }

   public void reset() {
       forAllChosen = false;
   }

   public String getComment() {
      return comment;
   }

   public boolean isForAllChosen() {
      return forAllChosen;
   }
}