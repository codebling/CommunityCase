package org.intellij.plugins.ui.common;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SingleRowSelectionTable extends JTable {

   public SingleRowSelectionTable() {
      this(new DefaultTableModel());
   }

   public SingleRowSelectionTable(TableModel tablemodel) {
      super(tablemodel);
      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent event) { SingleRowSelectionTable.this.mousePressed(event); }
      });
//        boolean flag = f.b;
   }

   private void mousePressed(MouseEvent event) {
      if (SwingUtilities.isRightMouseButton(event)) {
         int selectedRows[] = getSelectedRows();
         if (selectedRows.length < 2) {
            int i = rowAtPoint(event.getPoint());
            if (i != -1) {
               getSelectionModel().setSelectionInterval(i, i);
            }
         }
      }
   }

//    public void removeNotify()
//    {
//        KeyboardFocusManager keyboardfocusmanager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//        keyboardfocusmanager.removePropertyChangeListener("focusOwner", a);
//        super.removeNotify();
//    }
//
//    public boolean editCellAt(int i, int j, EventObject eventobject)
//    {
//        if(cellEditor != null && !cellEditor.stopCellEditing())
//            return false;
//        if(i < 0 || i >= getRowCount() || j < 0 || j >= getColumnCount())
//            return false;
//        if(!isCellEditable(i, j))
//            return false;
//        if(a == null)
//        {
//            KeyboardFocusManager keyboardfocusmanager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//            a = new dk(this, keyboardfocusmanager);
//            keyboardfocusmanager.addPropertyChangeListener("focusOwner", a);
//        }
//        TableCellEditor tablecelleditor = getCellEditor(i, j);
//        if(tablecelleditor != null && tablecelleditor.isCellEditable(eventobject))
//        {
//            editorComp = prepareEditor(tablecelleditor, i, j);
//            if(editorComp == null)
//            {
//                removeEditor();
//                return false;
//            } else
//            {
//                editorComp.setBounds(getCellRect(i, j, false));
//                add(editorComp);
//                editorComp.validate();
//                setCellEditor(tablecelleditor);
//                setEditingRow(i);
//                setEditingColumn(j);
//                tablecelleditor.addCellEditorListener(this);
//                return true;
//            }
//        } else
//        {
//            return false;
//        }
//    }
//
//    public void removeEditor()
//    {
//        KeyboardFocusManager keyboardfocusmanager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//        keyboardfocusmanager.removePropertyChangeListener("focusOwner", a);
//        super.removeEditor();
//    }

//    private dk a;
}
