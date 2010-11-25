package net.sourceforge.transparent.actions;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import net.sourceforge.transparent.ClearCaseException;
import net.sourceforge.transparent.ClearCaseFile;

import java.util.ArrayList;
import java.util.List;

public abstract class AsynchronousAction extends FileAction {

   abstract protected void perform(VirtualFile file, ActionContext context) throws VcsException;

   protected List runAction(final ActionContext context) {
      final VirtualFile file = context.files[0];

      final List exceptions = new ArrayList();

      try {
         perform(file, context);
      } catch (VcsException ex) {
         ex.setVirtualFile(file);
         exceptions.add(ex);
      } catch (ClearCaseException ex) {
         VcsException e = new VcsException(ex);
         e.setVirtualFile(file);
         exceptions.add(e);
      }

      return exceptions;
   }

   public static String getVersionExtendedPathName(VirtualFile file, ActionContext context){
      final ClearCaseFile clearCaseFile = new ClearCaseFile(file, context.vcs.getClearCase());
      if (clearCaseFile.isHijacked())
         return clearCaseFile.getPath()+"@@";
      else
         return clearCaseFile.getPath();

   }

}
