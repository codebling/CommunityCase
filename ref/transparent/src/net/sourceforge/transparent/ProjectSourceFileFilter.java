package net.sourceforge.transparent;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.plugins.util.FileFilter;
import org.intellij.plugins.util.FileUtil;

import java.io.File;

/**
 * User: sg426575
 * Date: Oct 2, 2003
 * Time: 1:56:06 PM
 */
public class ProjectSourceFileFilter implements FileFilter {
   private Project project;
   private FileUtil fileUtil;

   public ProjectSourceFileFilter(Project project) {
      this.project = project;
      this.fileUtil = new FileUtil();
   }

   public boolean accept(String path) {
      return accept(new File(path));
   }

   public boolean accept(File file) {
      return accept(fileUtil.ioFileToVirtualFile(file));
   }

   public boolean accept(VirtualFile file) {
      ModuleManager moduleManager = ModuleManager.getInstance(project);
      Module[] modules = moduleManager.getModules();
      for (int i = 0; i < modules.length; i++) {
         Module module = modules[i];
         ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
         if (moduleRootManager.getFileIndex().isInSourceContent(file)) {

         }


      }
      return false;
   }

}
