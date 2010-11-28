package org.intellij.plugins.ui.pathlisteditor;

import javax.swing.table.AbstractTableModel;

public class PathListTableModel extends AbstractTableModel
{

    PathListTableModel(PathListEditorPanel panel, String columnNames[])
    {
        this.panel = panel;
        this.columnNames = columnNames;
    }

    public int getColumnCount()
    {
        return columnNames.length;
    }

    public int getRowCount()
    {
        return panel.getPathListElements().size();
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        PathListElement elt = (PathListElement)panel.getPathListElements().get(rowIndex);
        if(columnIndex == 0)
            return elt;
        if(columnIndex == 1)
        {
            if(!elt.isFile())
                return elt.isIncludeSubDirectories() ? Boolean.TRUE : Boolean.FALSE;
            else
                return null;
        } else
        {
            return null;
        }
    }

    public String getColumnName(int columnIndex)
    {
        return columnNames[columnIndex];
    }

    public Class getColumnClass(int columnIndex)
    {
        if(columnIndex == 0)
            return Object.class;
        if(columnIndex == 1)
            return Boolean.class;
        else
            return null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        if(columnIndex == 1)
        {
            PathListElement elt = (PathListElement)panel.getPathListElements().get(rowIndex);
            return !elt.isFile();
        } else
        {
            return false;
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        PathListElement elt = (PathListElement)panel.getPathListElements().get(rowIndex);
        elt.setIncludeSubDirectories(aValue.equals(Boolean.TRUE));
    }

    private final String columnNames[];
    private final PathListEditorPanel panel;
}
