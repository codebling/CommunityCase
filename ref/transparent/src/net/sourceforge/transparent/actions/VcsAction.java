
package net.sourceforge.transparent.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.localVcs.LvcsAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.plugins.util.LogUtil;

import java.util.List;

public abstract class VcsAction extends AnAction {
   public static final Logger LOG = LogUtil.getLogger();

   public void update(AnActionEvent e) {
      ActionContext context = getActionContext(e);
      logAction("update", context);

      Presentation presentation = e.getPresentation();
      if (isVisible(context)) {
         presentation.setVisible(true);
         presentation.setEnabled(isEnabled(context));
         presentation.setText(getActionName(context));

      } else {
         presentation.setVisible(false);
      }
   }

   protected boolean isVisible(ActionContext c) {
      return c.isVcsActive();
   }

   protected boolean isEnabled(ActionContext c) {
      return true;
   }

   protected ActionContext getActionContext(AnActionEvent e) {
      return new ActionContext(e);
   }

   protected void logAction(String m, ActionContext c) {
      debug("enter: " + m + "(" + getActionName(c) + ", id='" + OpenApiFacade.getActionManager().getId(this) + "')");
      debug(c.toString());
   }

   // TODO: getActionName should not take a VCS as argument
   protected abstract String getActionName(ActionContext context);

   protected void debug(String message) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(message);
      }
   }

   public void actionPerformed(AnActionEvent event) {
      ActionContext c = getActionContext(event);
      logAction("actionPerformed", c);
      if (!isEnabled(c)) {return;}

      OpenApiFacade.getFileDocumentManager().saveAllDocuments();

      showExceptions(runAction(c), c);
   }

   protected List runAction(ActionContext context) {
      String actionName = getActionName(context);
/*@if Aurora@*/
      LvcsAction lvcsAction = null;
/*@end@*/
      if (actionName != null) {
/*@if Aurora@*/
         lvcsAction =
/*@end@*/
         context.vcsHelper.startVcsAction(actionName);
      }

      try {
         return execute(context);
      } finally {
         if (actionName != null) {
            /*@if Aurora@*/
            context.vcsHelper.finishVcsAction(lvcsAction);
            /*@else
             context.vcsHelper.finishVcsAction();
          /*@end@*/
         }
      }
   };

   protected List execute(ActionContext context){ return null;}

   protected void showExceptions(List exceptions, final ActionContext context) {
      if (exceptions != null && !exceptions.isEmpty()) {
         String actionName = getActionName(context);
         context.vcsHelper.showErrors(exceptions, actionName != null ? actionName : context.vcs.getName());
      }
   }
}
