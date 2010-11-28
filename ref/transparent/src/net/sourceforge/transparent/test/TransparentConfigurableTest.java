package net.sourceforge.transparent.test;

import junit.framework.TestCase;
import net.sourceforge.transparent.TransparentConfigurable;
import net.sourceforge.transparent.TransparentConfiguration;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.intellij.openapi.testing.MockProject;
import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 2:57:14 PM
 * To change this template use Options | File Templates.
 */
public class TransparentConfigurableTest extends TestCase implements PropertyChangeListener {
   private boolean notified;

   public void propertyChange(PropertyChangeEvent evt) {
      notified = true;
   }

   public void testApplyChangesToConfiguration() throws Exception {
      MockProject project = new MockProject();
      
      ExcludedPathsFromVcsConfiguration excludedPathsFromVcsConfiguration = new ExcludedPathsFromVcsConfiguration(project);
      project.addComponent(excludedPathsFromVcsConfiguration);
      TransparentConfiguration configuration = new TransparentConfiguration(project);
      project.addComponent(configuration);

      TransparentConfigurable configurable = new TransparentConfigurable(project) {
         protected String getClearCaseDefaultRoot() {return ""; }
      };
      configurable.init();
      configuration.addListener(this);
      configurable.applyChangesToConfiguration();
      assertTrue("no notification", notified);
   }
}
