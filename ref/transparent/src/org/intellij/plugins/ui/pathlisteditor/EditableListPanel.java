package org.intellij.plugins.ui.pathlisteditor;

import java.awt.*;
import javax.swing.*;

public abstract class EditableListPanel extends JPanel
{

    public EditableListPanel()
    {
        super(new GridBagLayout());
    }

    protected void initUI()
    {
        JComponent listPanel = getListPanel();
        JButton buttons[] = getButtons();
        String title = getTitle();
        if(title != null)
            add(new JLabel(title), new GridBagConstraints(0, 0, 2, 1, 1.0D, 0.0D, 17, 2, new Insets(0, 0, 4, 0), 0, 0));
        add(listPanel, new GridBagConstraints(0, 1, 1, buttons.length, 1.0D, 1.0D, 17, 1, new Insets(0, 0, 0, 4), 0, 0));
        for(int i = 0; i < buttons.length; i++)
        {
            JButton button = buttons[i];
            add(button, new GridBagConstraints(1, 1 + i, 1, 1, 0.0D, i != buttons.length - 1 ? 0 : 1, 11, 2, new Insets(0, 0, 4, 0), 0, 0));
        }

    }

    protected abstract String getTitle();

    protected abstract JButton[] getButtons();

    protected abstract JComponent getListPanel();
}
