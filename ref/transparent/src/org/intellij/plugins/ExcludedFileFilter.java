package org.intellij.plugins;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.intellij.plugins.ui.pathlisteditor.PathListElement;
import org.intellij.plugins.util.FileFilterAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcludedFileFilter extends FileFilterAdapter {
   private List excludedPaths      = new ArrayList();
   private List excludedExtensions = new ArrayList();

   private static final Logger LOG = Logger.getInstance("org.intellij.plugins.ExcludedFileFilter");

   public ExcludedFileFilter(List excludedPaths, List excludedExtensions) {
      this.excludedPaths      = new ArrayList(excludedPaths);
      this.excludedExtensions = new ArrayList(excludedExtensions);
   }

   public ExcludedFileFilter() {}

   public List getExcludedPaths() {
      return excludedPaths;
   }


   public List getExcludedExtensions() {
      return excludedExtensions;
   }

   public boolean accept(File file) {
      boolean isAccepted = !isFileInExcludedPaths(file) && !isFileExtensionExcluded(file);
      debug((isAccepted ? "Accepted" : "Filtered out") + " file " + file.getPath());
      return isAccepted;
   }

   private boolean isFileInExcludedPaths(File file) {
      for (Iterator iterator = excludedPaths.iterator(); iterator.hasNext();) {
         PathListElement excludedPath = (PathListElement) iterator.next();

         if (isFileExcluded(excludedPath, file)) { // todo Match using canonical paths. What does getPresentableUrl return?
            return true;
         }
      }
      return false;
   }

   private static boolean isFileExcluded(PathListElement excludedPath, File file) {
      return isFileExplicitlyExcluded(excludedPath, file) ||
             isFileInRecursiveExcludedDirectory(excludedPath, file) ||
             isFileParentExcluded(excludedPath, file);
   }

   private static boolean isFileParentExcluded(PathListElement excludedPath, File file) {
      return !excludedPath.isFile() &&
             !excludedPath.isIncludeSubDirectories() &&
             file.getParent().equals(excludedPath.getPresentableUrl());
   }

   private static boolean isFileInRecursiveExcludedDirectory(PathListElement excludedPath, File file) {
      return !excludedPath.isFile() &&
             excludedPath.isIncludeSubDirectories() &&
             file.getParent().startsWith(excludedPath.getPresentableUrl());
   }

   private static boolean isFileExplicitlyExcluded(PathListElement excludedPath, File file) {
      return getCanonicalFilePath(file).equals(excludedPath.getPresentableUrl());
   }

   private static String getCanonicalFilePath(File file) {
      try {
         return file.getCanonicalPath();
      } catch (IOException e) {
         return "";
      }
   }

   private boolean isFileExtensionExcluded(File file) {
      boolean hasExcludedExtension = false;
      for (int i = 0; i < excludedExtensions.size(); i++) {
         if (file.getName().endsWith((String) excludedExtensions.get(i))) {
            hasExcludedExtension = true;
         }
      }
      return hasExcludedExtension;
   }

   public String toString()
   {
      return new ToStringBuilder(this).
            append("exludedPaths", excludedPaths).
            append("exludedExtensions", excludedExtensions).
            toString();
   }

   private void debug(String message) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(message);
      }
   }
}
