package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.localVcs.LocalVcs;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectDialog;

import javax.swing.*;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: sg426575
 * Date: Jul 31, 2003
 * Time: 3:44:00 PM
 * To change this template use Options | File Templates.
 */
public class CheckInProjectDialog extends CheckinProjectDialog implements CheckInFields {
   private CheckInEnvironment env;

   public CheckInProjectDialog(Project project, CheckInEnvironment checkInEnvironment) {
      super(project,
            "Check In Project",
            true,
            checkInEnvironment,
            Arrays.asList(LocalVcs.getInstance(project).getRootPaths()));
      env = checkInEnvironment;
   }

   public JComponent getPreferredFocusedComponent() {
      return getOptionsPanel().getScrField();
   }

   private CheckInOptionsPanel getOptionsPanel() {
      return (CheckInOptionsPanel) env.getAdditionalOptionsPanel();
   }

   public void setScr(String scr) {
      getOptionsPanel().setScr(scr);
   }

   public String getScr() {
      return getOptionsPanel().getScr();
   }

   public void setShowScrField(boolean showField) {
      getOptionsPanel().setShowScrField(showField);
   }

}
