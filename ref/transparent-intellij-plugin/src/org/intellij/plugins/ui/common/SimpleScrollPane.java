package org.intellij.plugins.ui.common;

import javax.swing.*;
import javax.swing.plaf.ScrollPaneUI;
import java.awt.Component;

public class SimpleScrollPane extends JScrollPane {

   public SimpleScrollPane(JComponent component) {
      super(component);
   }

   public SimpleScrollPane() { }

   public void setUI(ScrollPaneUI ui) {
      super.setUI(ui);
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            Component component = getViewport().getView();
            if (component != null) {
               getViewport().setBackground(component.getBackground());
            }
         }
      });
   }
}
