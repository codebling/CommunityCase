package com.liquidnet.ideaplugin.mks;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

class CheckOutDialog extends DialogWrapper {

    public static final int MERGE = OK_EXIT_CODE;
    public static final int OVERWRITE = NEXT_USER_EXIT_CODE;
    public static final int OVERWRITE_ALL = NEXT_USER_EXIT_CODE + 1;
    public static final int SKIP = NEXT_USER_EXIT_CODE + 2;
    public static final int CANCEL = CANCEL_EXIT_CODE;

    private String _fileName;

    public CheckOutDialog(Project project, String fileName) { //pp
        super(project, false);
        _fileName = fileName;
        init();
    }

    public String getTitle() {
        return "Check-Out Conflict";
    }

    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(new JLabel(_fileName));
        panel.add(new JLabel("Working file has changes.  What do you want to do?"));
        return panel;
    }

    protected Action[] createActions() {
        setOKButtonText("Merge");
        setCancelButtonText("Cancel All");

        Action overwrite = new AbstractAction("Overwrite") {
            public void actionPerformed(ActionEvent actionevent) {
                close(OVERWRITE);
            }
        };
        Action overwrite_all = new AbstractAction("Overwrite All") {
            public void actionPerformed(ActionEvent actionevent) {
                close(OVERWRITE_ALL);
            }
        };
        Action skip = new AbstractAction("Skip") {
            public void actionPerformed(ActionEvent actionevent) {
                close(SKIP);
            }
        };
        getOKAction().putValue(DEFAULT_ACTION, Boolean.FALSE);
        getCancelAction().putValue(DEFAULT_ACTION, Boolean.TRUE);
        return new Action[]{getOKAction(), overwrite, overwrite_all, skip, getCancelAction()};
    }
}