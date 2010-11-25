package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.VcsException;

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
public abstract class BaseCheckInDialog extends DialogWrapper {
//    protected JTextField commentArea;
   protected           JTextArea  commentArea;
   protected           JTextField scrField;
   protected           Project    project;
   private             JPanel     scrFieldPanel;
   public static final int        OK_ALL_EXIT_CODE = NEXT_USER_EXIT_CODE;

   public BaseCheckInDialog(Project project) {
      super(project, false);
      this.project = project;
//        commentArea = new JTextField();
      commentArea = new JTextArea();
      scrField    = new JTextField();
   }

   protected void dispose() {
      super.dispose();
   }

   public JPanel createCheckInInfoPanel() {

      scrFieldPanel = new JPanel();
      getScrFieldPanel().setLayout(new BorderLayout());
      getScrFieldPanel().add(new Label("SCR Number"), BorderLayout.NORTH);
      getScrFieldPanel().add(getScrField(), BorderLayout.CENTER);

      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.add(getScrFieldPanel(), BorderLayout.NORTH);
      panel.add(new Label("Comments"), BorderLayout.CENTER);
      panel.add(getCommentArea(), BorderLayout.SOUTH);
      getCommentArea().setRows(3);
      getCommentArea().setLineWrap(true);

      panel.validate();
      return panel;
   }

   public JComponent createCenterPanel() {
      JPanel panel = new JPanel();
      panel.setMinimumSize(new Dimension(650, 50));
      panel.setLayout(new BorderLayout());
      panel.add(createCheckInInfoPanel(), BorderLayout.SOUTH);

      scrField.addFocusListener(new FocusListener() {
         public void focusGained(FocusEvent e) {
            scrField.selectAll();
         }

         public void focusLost(FocusEvent e) {
         }
      });

      commentArea.addFocusListener(new FocusListener() {
         public void focusGained(FocusEvent e) {
            commentArea.selectAll();
         }

         public void focusLost(FocusEvent e) {
         }
      });

      return panel;
   }

   public boolean shouldShowDialog() {
      return true;
   }

   public JTextArea getCommentArea() {
      return commentArea;
   }

   public void setCommentArea(JTextArea commentArea) {
      this.commentArea = commentArea;
   }

   public JTextField getScrField() {
      return scrField;
   }

   public void setScrField(JTextField scrField) {
      this.scrField = scrField;
   }

   public JComponent getPreferredFocusedComponent() {
      return getCommentArea(); //TOTEST: comment area has focus
   }

   public void setComment(String comment) {
      getCommentArea().setText(comment);
      getCommentArea().setSelectionStart(0);
      getCommentArea().setSelectionEnd(comment.length());
      getCommentArea().setCaretPosition(comment.length());
   }

   public void setScr(String scr) {
      getScrField().setText(scr);
   }

   public void setShowScrField(boolean showField) {
      getScrFieldPanel().setVisible(showField);
   }

   public JPanel getScrFieldPanel() {
      return scrFieldPanel;
   }
}
