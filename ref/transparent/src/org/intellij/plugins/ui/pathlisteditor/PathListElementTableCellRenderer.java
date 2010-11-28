package org.intellij.plugins.ui.pathlisteditor;

import org.intellij.plugins.ui.common.ElipsisLabelUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class PathListElementTableCellRenderer extends DefaultTableCellRenderer {

   public PathListElementTableCellRenderer() {
      setUI(new ElipsisLabelUI());
   }

   public Component getTableCellRendererComponent(JTable table,
                                                  Object value,
                                                  boolean isSelected,
                                                  boolean hasFocus,
                                                  int row,
                                                  int column) {
      Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (value instanceof PathListElement) {
         PathListElement elt = (PathListElement) value;
         setText(elt.getPresentableUrl());
         if (!elt.isValid()) {
            setForeground(Color.RED);
         }
      }
      if (!isSelected) {
         setBackground(table.getBackground());
      }
      return component;
   }
}
