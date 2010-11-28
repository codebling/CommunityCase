package net.sourceforge.transparent.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import net.sourceforge.transparent.Runner;

import java.util.List;

public class MergeProjectAction extends VcsAction {

   protected List execute(ActionContext ct) {
      new Runner().run("clearmrgman");
      return null;
   }

   protected String getActionName(ActionContext context) {
      return "Merge Project";
   }
}

