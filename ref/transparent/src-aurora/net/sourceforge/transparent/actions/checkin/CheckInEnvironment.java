package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.localVcs.LocalVcsServices;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ColumnInfo;
import com.intellij.openapi.vcs.DifferencesProvider;
import com.intellij.openapi.vcs.RollbackProvider;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.checkin.DifferenceType;
import com.intellij.openapi.vcs.checkin.RevisionsFactory;
import com.intellij.openapi.vcs.ui.Refreshable;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 * User: sg426575
 * Date: Jul 31, 2003
 * Time: 4:26:32 PM
 * To change this template use Options | File Templates.
 */
public class CheckInEnvironment implements CheckinEnvironment {
   private CheckInConfig configuration;
   private CheckinEnvironment lvcsCheckinEnvironment;
   private CheckInOptionsPanel optionsPanel;

   public CheckInEnvironment(Project project, CheckInConfig configuration) {
      this.configuration = configuration;
      this.optionsPanel = new CheckInOptionsPanel();
      LocalVcsServices lvcsServices = LocalVcsServices.getInstance(project);
      if (lvcsServices != null)
         this.lvcsCheckinEnvironment = lvcsServices.createCheckinEnvironment();
   }

   public RevisionsFactory getRevisionsFactory() {
      return lvcsCheckinEnvironment.getRevisionsFactory();
   }

   public DifferencesProvider createDifferencesProviderOn(Project project, VirtualFile virtualFile) {
      if (virtualFile == null) return null;
      return lvcsCheckinEnvironment.createDifferencesProviderOn(project, virtualFile);
   }

   public RollbackProvider createRollbackProviderOn(DataContext provider) {
      return lvcsCheckinEnvironment.createRollbackProviderOn(provider);
   }

   public DifferenceType[] getAdditionalDifferenceTypes() {
      return lvcsCheckinEnvironment.getAdditionalDifferenceTypes();
   }

   public ColumnInfo[] getAdditionalColumns(int index) {
      return lvcsCheckinEnvironment.getAdditionalColumns(index);
   }

   public RefreshableOnComponent createAdditionalOptionsPanel(Refreshable panel, boolean checkinProject) {
      return getAdditionalOptionsPanel();
   }

   public RefreshableOnComponent createAdditionalOptionsPanelForCheckinProject(Refreshable panel) {
      return null;
   }

   public RefreshableOnComponent createAdditionalOptionsPanelForCheckinFile(Refreshable panel) {
      return null;
   }

   public String getDefaultMessageFor(VirtualFile[] filesToCheckin) {
      return null;
   }

   public CheckInOptionsPanel getAdditionalOptionsPanel() {
      return optionsPanel;
   }

   public VcsConfiguration getConfiguration() {
      return configuration;
   }

   public void onRefreshFinished() {
   }

   public void onRefreshStarted() {
   }

   public AnAction[] getAdditionalActions(int index) {
      return new AnAction[0];
   }
}
