package net.sourceforge.transparent.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.plugins.actions.NopAction;

public class CheckOutOrHijackAction extends AnAction {

   public void update(AnActionEvent e) {
      getDelegatedAction(e).update(e);
   }

   private AnAction getDelegatedAction(AnActionEvent e) {
      ActionContext actionContext = new ActionContext(e);
      AnAction action;
      if (!actionContext.isVcsActive()) {
         action = NopAction.getInstance();
      } else if (actionContext.vcs.getTransparentConfig().offline) {
         action = OpenApiFacade.getActionManager().getAction("ClearCase.Hijack");
      } else {
         action = OpenApiFacade.getActionManager().getAction("ClearCase.CheckOut");
      }
      return action;
   }

   public void actionPerformed(AnActionEvent e) {
      getDelegatedAction(e).actionPerformed(e);
   }

}

