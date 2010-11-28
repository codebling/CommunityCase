package org.intellij.plugins.ui.common;

import java.awt.Component;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class CheckBoxTableCellRenderer extends JCheckBox implements TableCellRenderer {
   public CheckBoxTableCellRenderer() {
      panel = new JPanel();
      setHorizontalAlignment(0);
   }

   public Component getTableCellRendererComponent(JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column) {
      if (value == null) {
         if (isSelected)
            panel.setBackground(table.getSelectionBackground());
         else
            panel.setBackground(table.getBackground());
         return panel;
      }
      if (isSelected) {
         setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
      } else {
         setForeground(table.getForeground());
         setBackground(table.getBackground());
      }
      setSelected(((Boolean) value).booleanValue());
      return this;
   }

   private JPanel panel;
}
