package net.sourceforge.transparent.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.VcsManager;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.Editor;

import java.util.Arrays;

import net.sourceforge.transparent.TransparentVcs;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.openapi.DataContextUtil;

public class ActionContext {
   public Project project;
   public TransparentVcs vcs;
   public AbstractVcsHelper vcsHelper;
   public VirtualFile[] files;
   public DataContext dataContext;
   public FileStatusManager fileStatusManager;
   public boolean isActionRecursive;
   public AnActionEvent event;

   public ActionContext() {
   } // for testing only

   public ActionContext(AnActionEvent e) {
      event = e;
      dataContext = e.getDataContext();
      project = DataContextUtil.getProject(event);

      if (!isVcsActive(e)) return;

      vcs = (TransparentVcs) VcsManager.getInstance(project).getActiveVcs();
      vcsHelper = AbstractVcsHelper.getInstance(project);
      fileStatusManager = OpenApiFacade.getFileStatusManager(project);
      files = DataContextUtil.getVirtualFileArray(event);
      if (files==null && DataContextUtil.getVirtualFile(event) != null) files= new VirtualFile[] {DataContextUtil.getVirtualFile(event)};
   }


   public boolean isVcsActive() {return project!=null && vcs!= null;}

   public static boolean isVcsActive(AnActionEvent e) {
      Project project = DataContextUtil.getProject(e);
      if (project == null) return false;
      VcsManager vcsMgr = VcsManager.getInstance(project);
      if (vcsMgr == null) return false;
      return vcsMgr.getActiveVcs() != null && vcsMgr.getActiveVcs() instanceof TransparentVcs;
   }

   public String toString() {
      if (!isVcsActive()) return "TransparentVcs unactivated";
      if (event == null) return "";
      StringBuffer buf = new StringBuffer();
      buf.append("PROJECT="+DataContextUtil.getProject(event));
      buf.append(",\nEDITOR="+DataContextUtil.getEditor(event));
      buf.append(",\nFILE="+DataContextUtil.getVirtualFile(event));
      buf.append(",\nFILE_ARRAY=");
      VirtualFile[] files = DataContextUtil.getVirtualFileArray(event);
      if (files == null)
         buf.append("null");
      else
         buf.append("'" + Arrays.asList(files) + "'");
      return buf.toString();
   }


   public String getVcsName() {
      String name = "Unknown VCS";
      if (vcs != null)
         name = vcs.getName();
      return name;
   }
}
