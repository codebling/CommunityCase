package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diff.DiffPanelFactory;
import com.intellij.openapi.diff.DiffPanel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.WindowManager;

import java.io.IOException;

import net.sourceforge.transparent.Runner;
import net.sourceforge.transparent.ClearCaseException;

public class DifferencesAction extends AsynchronousAction {

   public void perform(VirtualFile file, ActionContext context) {

//      cleartool("diff -g -pre "+ file.getPresentableUrl());

    DiffDialog diffDialog = new DiffDialog(context.project, file);
    diffDialog.show();
//    diffPanel.
   }

   protected String getActionName(ActionContext context) {
      return "Differences";
   }

}
