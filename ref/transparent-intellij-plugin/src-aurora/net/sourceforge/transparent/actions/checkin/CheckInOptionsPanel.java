package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Mar 12, 2003
 * Time: 2:26:54 PM
 * To change this template use Options | File Templates.
 */
public class CheckInOptionsPanel extends JPanel implements RefreshableOnComponent {
   protected           JTextField scrField;
   protected           Project    project;

   public CheckInOptionsPanel() {
      super();
      scrField    = new JTextField();

      setLayout(new BorderLayout());
      add(new Label("SCR Number"), BorderLayout.NORTH);
      add(getScrField(), BorderLayout.CENTER);

      scrField.addFocusListener(new FocusListener() {
         public void focusGained(FocusEvent e) {
            scrField.selectAll();
         }

         public void focusLost(FocusEvent e) {
         }
      });
   }

   public JTextField getScrField() {
      return scrField;
   }

   public void setScrField(JTextField scrField) {
      this.scrField = scrField;
   }

   public void setScr(String scr) {
      getScrField().setText(scr);
   }

   public String getScr() {
      return getScrField().getText();
   }

   public void setShowScrField(boolean showField) {
      this.setVisible(showField);
   }

   public boolean isShowScrField() {
      return this.isVisible();
   }

   public JComponent getComponent() {
      return this;
   }

   public void refresh() {

   }

   public void saveState() {

   }

   public void restoreState() {

   }
}
