/*
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: Sep 18, 2002
 * Time: 1:22:29 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package net.sourceforge.transparent.actions;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.intellij.openapi.DataContextUtil;
import net.sourceforge.transparent.Version;

public class AboutAction extends AnAction {
   public void actionPerformed(AnActionEvent e) {
      Messages.showMessageDialog(DataContextUtil.getProject(e),
                                 "Clearcase plugin version " + new Version().getVersion(),
                                 "Clearcase Plugin",
                                 Messages.getInformationIcon());
   }
}
