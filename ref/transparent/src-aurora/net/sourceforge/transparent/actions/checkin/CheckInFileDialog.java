package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.CheckinFileDialog;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * User: Jacques
 * Date: Aug 18, 2003
 * Time: 9:42:09 PM
 */
public class CheckInFileDialog extends CheckinFileDialog implements CheckInFields {
   public static final int        OK_ALL_EXIT_CODE = NEXT_USER_EXIT_CODE;

   private Action[] actionButtons;
   private CheckInEnvironment env;

   public void setScr(String scr) {
      getOptionsPanel().setScr(scr);
   }

   public String getScr() {
      return getOptionsPanel().getScr();
   }

   public void setShowScrField(boolean showField) {
      getOptionsPanel().setShowScrField(showField);
   }

   public boolean isShowScrField() {
      return getOptionsPanel().isShowScrField();
   }

   public CheckInFileDialog(Project project, VirtualFile[] files, CheckInEnvironment env, int modifiers) {
      super(project, env, files, modifiers);
      this.env = env;
      if (files.length > 0)
         setTitle("Check In " + files[0]);
   }

   private Action[] getActionButtons() {
      Action okAll = new AbstractAction("OK to All") {
         public void actionPerformed(ActionEvent e) {
            if (isEnabled()) {
               close(OK_ALL_EXIT_CODE);
            }
         }
      };

      return new Action[]{getOKAction(), okAll, getCancelAction()};
   }

   protected Action[] createActions() {
      if (actionButtons == null) {
         actionButtons = getActionButtons();
      }
      return actionButtons;
   }

   private CheckInOptionsPanel getOptionsPanel() {
      return env.getAdditionalOptionsPanel();
   }

}
