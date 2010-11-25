package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import net.sourceforge.transparent.actions.checkin.BaseCheckInDialog;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Feb 24, 2003
 * Time: 12:13:41 PM
 * To change this template use Options | File Templates.
 */
public class CheckInFileDialog extends BaseCheckInDialog {
   private Action[] actionButtons;

   public CheckInFileDialog() {
      super(null);
   }

   public CheckInFileDialog(Project project) {
      super(project);
      Action okAll = new AbstractAction("OK to All") {
         public void actionPerformed(ActionEvent actionevent) {
            if (isEnabled()) {
               close(OK_ALL_EXIT_CODE);
            }
         }
      };

      actionButtons = new Action[]{getOKAction(), okAll, getCancelAction()};

      init();
   }

   protected Action[] createActions() {
      return actionButtons;
   }

   public void setFileName(String presentableUrl) {
      setTitle("Check In File : " + presentableUrl);
   }

}
