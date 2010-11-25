package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;

import javax.swing.*;
import java.awt.*;

import net.sourceforge.transparent.actions.checkin.BaseCheckInDialog;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Feb 24, 2003
 * Time: 12:13:41 PM
 * To change this template use Options | File Templates.
 */
public class CheckInProjectDialog extends BaseCheckInDialog {

   private CheckinProjectPanel checkinProjectPanel = null;

   public CheckInProjectDialog(Project project, CheckinProjectPanel checkinProjectPanel) {
      super(project);
      setTitle("Check In Project");
      this.checkinProjectPanel = checkinProjectPanel;
      init();
   }

   public JComponent createNorthPanel() {
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(getCheckinProjectPanel().getComponent(), BorderLayout.CENTER);

      return panel;
   }

   public CheckinProjectPanel getCheckinProjectPanel() {
      return checkinProjectPanel;
   }
}
