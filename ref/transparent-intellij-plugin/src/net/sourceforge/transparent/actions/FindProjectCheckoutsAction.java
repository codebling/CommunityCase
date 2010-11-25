package net.sourceforge.transparent.actions;

import net.sourceforge.transparent.TransparentVcs;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;


public class FindProjectCheckoutsAction extends FindCheckoutsAction {
   protected String getTargetPath(TransparentVcs vcs, String filePath) {
      return vcs.getTransparentConfig().clearcaseRoot;
   }
}

