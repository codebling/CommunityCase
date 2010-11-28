package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.ClearCaseException;
import net.sourceforge.transparent.Runner;
import org.intellij.openapi.OpenApiFacade;

import java.io.IOException;

public abstract class FileAction extends VcsAction {

   protected boolean hasFileTarget(ActionContext c) {
      return c.files != null && c.files.length != 0;
   }

   public boolean isEnabled(ActionContext context) {

//        if (context.event.getInputEvent() != null) return false;
      //
      if (!hasFileTarget(context)) {
         debug("Action " + getActionName(context) + " disable: " + (context.files == null ? "files=null" : "0 files"));
         return false;
      }

      boolean enabled = false;

      for (int i = 0; i < context.files.length; i++) {
         VirtualFile file = context.files[i];
         try {
            if (isEnabled(file, context)) {
               enabled = true;
            }
         } catch (VcsException e1) {
            // Ignore VcsException for Presentation purposes.
         }
      }
      logActionState(enabled, context);

      return enabled;
   }

   private void logActionState(boolean enabled, ActionContext context) {
      if (!enabled) {
         debug("Action " + getActionName(context) + " disable: all files are ADDED (not commited to CC yet)");
      } else
         debug("Action " + getActionName(context) + " enable");
   }

   protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
      debug("project=" + context.project + ", file=" + file);
      return OpenApiFacade.getFileStatusManager(context.project).getStatus(file) != FileStatus.ADDED;
   }

   public void cleartool(String[] subcmd) throws ClearCaseException {
      try {
         new Runner().runAsynchronously(Runner.getCommand("cleartool",subcmd));
      } catch (IOException e) {
         LOG.error(e);
         throw new ClearCaseException(e.getMessage());
      }
   }

}

