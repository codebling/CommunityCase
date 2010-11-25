package org.intellij.plugins.test;

import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 2:46:20 PM
 * To change this template use Options | File Templates.
 */
public class ExcludedPathsFromVcsConfigurationTest extends ListenerNotifierTest {
   ExcludedPathsFromVcsConfiguration configuration;
   protected void setUp() throws Exception {
      configuration = new ExcludedPathsFromVcsConfiguration(null);
      setConfiguration(configuration);
      super.setUp();
   }

   public void testResetExcludedPaths() throws Exception {
      configuration.resetExcludedPaths(new ArrayList());
      assertTrue("notified", notified);
   }

}
