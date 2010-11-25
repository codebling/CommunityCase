package net.sourceforge.transparent.test;

import net.sourceforge.transparent.TransparentConfiguration;
import org.intellij.plugins.test.ListenerNotifierTest;
import org.intellij.openapi.testing.MockProject;

/**
 * Created by IntelliJ IDEA.
 * User: Jacques
 * Date: May 22, 2003
 * Time: 1:48:41 PM
 * To change this template use Options | File Templates.
 */
public class TransparentConfigurationTest extends ListenerNotifierTest {
   protected void setUp() throws Exception {
      setConfiguration(new TransparentConfiguration(new MockProject()));
      super.setUp();
   }

}
