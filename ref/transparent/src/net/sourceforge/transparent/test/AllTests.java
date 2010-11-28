package net.sourceforge.transparent.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.intellij.openapi.testing.AllTestCase;

public class AllTests extends AllTestCase {
   static Class[] excludedTests = {
      NativeClearCaseTest.class,
      NewNativeClearCaseTest.class,
      NewCommandLineClearCaseTest.class,
      RunnerTest.class
   };

	public static TestSuite suite() throws Exception {
      TestSuite suite = new TestSuite();
      suite.addTest(getModuleSuite());
      suite.addTest(AllTestCase.getUtilModuleSuite(excludedTests));
      return suite;
	}

   public static Test getModuleSuite() throws Exception {
      return AllTestCase.getModuleSuite(excludedTests);
   }

}
