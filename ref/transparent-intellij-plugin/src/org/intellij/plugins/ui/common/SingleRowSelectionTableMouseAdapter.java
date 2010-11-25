package org.intellij.plugins.ui.common;

import org.intellij.plugins.ui.common.SingleRowSelectionTable;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

final class SingleRowSelectionTableMouseAdapter extends MouseAdapter
{

    SingleRowSelectionTableMouseAdapter(SingleRowSelectionTable table)
    {
        this.table = table;
    }

    public void mousePressed(MouseEvent event)
    {
        if(SwingUtilities.isRightMouseButton(event))
        {
            int selectedRows[] = table.getSelectedRows();
            if(selectedRows.length < 2)
            {
                int i = table.rowAtPoint(event.getPoint());
                if(i != -1)
                    table.getSelectionModel().setSelectionInterval(i, i);
            }
        }
    }

    private final SingleRowSelectionTable table;
}
