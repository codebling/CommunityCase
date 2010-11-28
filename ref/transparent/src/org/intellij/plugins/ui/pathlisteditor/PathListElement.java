package org.intellij.plugins.ui.pathlisteditor;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.openapi.OpenApiFacade;
import org.jdom.Element;

import java.io.File;

public class PathListElement {

   public PathListElement(VirtualFile virtualFile, boolean includeSubDirectories, boolean isFile) {
      init(new PersistentVirtualFile(virtualFile), includeSubDirectories, isFile);
   }

   public PathListElement(String url, boolean includeSubDirectories, boolean isFile) {
      init(new PersistentVirtualFile(url), includeSubDirectories, isFile);
   }

   private void init(PersistentVirtualFile persistentVirtualFile, boolean includeSubDirectories, boolean isFile) {
      this.persistableFile       = persistentVirtualFile;
      this.includeSubDirectories = includeSubDirectories;
      this.isFile                = isFile;
   }

   public PathListElement cloneMyself() {
      return new PathListElement(getUrl(), includeSubDirectories, isFile);
   }

   public boolean isFile() {
      return isFile;
   }

   public String getUrl() {
      return persistableFile.getUrl();
   }

   public String getPresentableUrl() {
      return persistableFile.getPath();
   }

   public boolean isIncludeSubDirectories() {
      return includeSubDirectories;
   }

   public void setIncludeSubDirectories(boolean flag) {
      includeSubDirectories = flag;
   }

   public VirtualFile getVirtualFile() {
      return persistableFile.getVirtualFile();
   }

   public boolean isValid() {
      return persistableFile.isValid();
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof PathListElement))
         return false;
      PathListElement element = (PathListElement) obj;
      if (element.isFile != isFile)
         return false;
      if (element.includeSubDirectories != includeSubDirectories)
         return false;
      return Comparing.equal(element.getUrl(), getUrl());
   }

   public int hashCode() {
      return getUrl().hashCode();
   }

   public String toString() {
      StringBuffer buf = new StringBuffer();
      buf.append("(");
      buf.append(isFile?"":"<d>");
      buf.append(includeSubDirectories?"<r>":"");
      buf.append(":");
      buf.append(persistableFile);
      buf.append(")");
      return buf.toString();
   }
   static void debug(String message) {
      if (LOG.isDebugEnabled()) {
         LOG.debug(message);
      }
   }

   private boolean               isFile;
   private boolean               includeSubDirectories;
   private PersistentVirtualFile persistableFile;

   private static final Logger LOG = Logger.getInstance("org.intellij.plugins.ui.pathlisteditor.PathListElement");


   private static class PersistentVirtualFile implements JDOMExternalizable {
      private String      url;
      private VirtualFile virtualFile;

      public PersistentVirtualFile(VirtualFile file) {
         assert file != null;
         this.url         = file.getPresentableUrl();
         this.virtualFile = file;
      }

      public PersistentVirtualFile(String url) {
         this.url         = url;
         this.virtualFile = OpenApiFacade.getLocalFileSystem().findFileByPath(url.replace(File.separatorChar, '/'));
         if (this.virtualFile == null) {
            debug(url + " cannot be found in local file system");
         }
      }

      public VirtualFile getVirtualFile() {return virtualFile; }

      public String getUrl() {return getPath(); }

      public String getPath() {return url; }

      public boolean isValid() {return virtualFile != null; }

      public void readExternal(Element element) throws InvalidDataException { }

      public void writeExternal(Element element) throws WriteExternalException { }

      public String toString()
      {
         return url;
      }
   }
}
