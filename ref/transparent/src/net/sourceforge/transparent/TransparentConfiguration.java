package net.sourceforge.transparent;

import com.intellij.openapi.components.BaseComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.DefaultJDOMExternalizer;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.application.ApplicationInfo;
import org.intellij.plugins.ListenerNotifier;
import org.jdom.Element;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;

/**
 * this is the persistent state of the transparent plugin - just anything that needs to be persisted as a field
 */
public class TransparentConfiguration implements ListenerNotifier, JDOMExternalizable, ProjectComponent {
   public String implementation = CommandLineClearCase.class.getName();
   public String clearcaseRoot = "";
   public boolean checkoutReserved = false;
   public boolean automaticCheckout = false;
   public boolean markExternalChangeAsUpToDate = true;
   public boolean checkInUseHijack = true;
   public boolean offline = false;
   private PropertyChangeSupport listenerSupport = new PropertyChangeSupport(this);
   private Field markExternalChangesAsUpToDateField;
   private BaseComponent lvcsConfiguration;
   private Project project;
   private static final String MARK_EXTERNAL_CHANGES_AS_UP_TO_DATE_FIELD = "MARK_EXTERNAL_CHANGES_AS_UP_TO_DATE";

   public void readExternal(Element element) throws InvalidDataException {
      DefaultJDOMExternalizer.readExternal(this, element);
   }

   public void writeExternal(Element element) throws WriteExternalException {
      DefaultJDOMExternalizer.writeExternal(this, element);
   }

   public TransparentConfiguration(Project project) {
      this.project = project;
   }

   public void projectOpened() {
       LOG.debug("projectOpened");
   }

   public void projectClosed() {
   }

   public void initComponent() {
       logConfig();
       initExternalChangesAreUpToDateField();
   }

    private void logConfig() {
        LOG.debug("##### Loading " + TransparentVcs.class.getName() + " version " + new Version().getVersion() + "###########");
        LOG.debug("#####    implementation        = " + implementation);
        LOG.debug("#####    clearcaseRoot         = " + clearcaseRoot);
        LOG.debug("#####    checkoutReserved      = " + checkoutReserved);
        LOG.debug("#####    automaticCheckout     = " + automaticCheckout);
        LOG.debug("#####    externalChangeUpToDate= " + markExternalChangeAsUpToDate);
        LOG.debug("#####    checkInUseHijack      = " + checkInUseHijack);
        LOG.debug("#####    offline               = " + offline);
    }

    private void initExternalChangesAreUpToDateField() {
      if (project == null || !isAriadna()) {return;}
      lvcsConfiguration = getLvcsConfiguration(project);
      if (lvcsConfiguration != null) {
         markExternalChangesAsUpToDateField = getMarkExternalChangesAsUpToDateField(lvcsConfiguration);
         resetLcvsConfiguration();
      } else {
         LOG.debug("Found no LvcsConfiguration. MarkExternalChangesAsUpToDate won't work");
      }
   }

   private boolean isAriadna() {
      ApplicationInfo info = ApplicationInfo.getInstance();
      LOG.debug(
         "##### IDEA Used = " +
         info.getVersionName() +
         " " +
         info.getMajorVersion() +
         "." +
         info.getMinorVersion() +
         " " +
         info.getBuildNumber());
      return info.getVersionName().equals("Ariadna");
   }

   public void disposeComponent() {
   }

   public String getComponentName() {
      return "TransparentConfiguration";
   }

   public static TransparentConfiguration getInstance(Project project) {
      return (TransparentConfiguration) project.getComponent(TransparentConfiguration.class);
   }

   public PropertyChangeListener[] getListeners() {
      return listenerSupport.getPropertyChangeListeners();
   }

   public void addListener(PropertyChangeListener listener) {
      listenerSupport.addPropertyChangeListener(listener);
   }

   public void notifyListenersOfChange() {
      logConfig();
      listenerSupport.firePropertyChange("configuration", null, this);
      resetLcvsConfiguration();
   }

   public void removeListener(PropertyChangeListener listener) {
      listenerSupport.removePropertyChangeListener(listener);
   }

   public static BaseComponent getLvcsConfiguration(Project project) {
      Object[] components = project.getComponents(Object.class);
      if (components == null) {return null;}
      for (int i = 0; i < components.length; i++) {
         Object c = components[i];
         if (BaseComponent.class.isAssignableFrom(c.getClass())) {
            BaseComponent bc = (BaseComponent) c;
            if (bc.getComponentName().equals("LvcsConfiguration") ||
                bc.getComponentName().equals("LvcsProjectConfiguration")) {
               return bc;
            }
         }
      }
      LOG.debug("Could not find LvcsConfiguration");
      return null;
   }

   public static Field getMarkExternalChangesAsUpToDateField(BaseComponent lvcsConfiguration) {
      try {
         return lvcsConfiguration.getClass().getField(MARK_EXTERNAL_CHANGES_AS_UP_TO_DATE_FIELD);
      } catch (NoSuchFieldException e) {
         LOG.debug("Could not find field " + MARK_EXTERNAL_CHANGES_AS_UP_TO_DATE_FIELD);
      } catch (SecurityException e) {
         LOG.debug("Could not access field " + MARK_EXTERNAL_CHANGES_AS_UP_TO_DATE_FIELD);
      }
      return null;
   }

   private void resetLcvsConfiguration() {
      if (markExternalChangesAsUpToDateField != null) {
         try {
            markExternalChangesAsUpToDateField.setBoolean(lvcsConfiguration, markExternalChangeAsUpToDate);
         } catch (SecurityException e) {
            LOG.debug(e);
         } catch (IllegalAccessException e) {
            LOG.debug(e);
         }
      }
   }

   private static final Logger LOG = Logger.getInstance("net.sourceforge.transparent.TransparentConfiguration");

   public String[] getAvailableImplementations() {
      String[] implementations = {
//         NewCommandLineClearCase.class.getName(),
         CommandLineClearCase.class.getName(),
         MockClearCase.class.getName(),
         "net.sourceforge.transparent.NativeClearCase",
         "net.sourceforge.transparent.NewNativeClearCase",
         TestClearCase.class.getName()
      };
      return implementations;
   }

}
