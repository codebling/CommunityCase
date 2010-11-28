package net.sourceforge.transparent.test;

import junit.framework.TestCase;
import net.sourceforge.transparent.Runner;

import java.io.File;
import java.util.ResourceBundle;
import java.net.InetAddress;

public class RunnerTest extends TestCase {
   private String testDirPath;

   public RunnerTest(String s) {
		super(s);
	}

	public void testRunner() {
		Runner runner = new Runner();
		runner.run("ant.bat -version");
		assertTrue(runner.getOutput().toString().indexOf("Ant") != -1);
	}

	public void testClearTool() throws Exception {
		Runner runner = new Runner();
		assertTrue("cleartool should have passed", runner.runCanFail("cleartool ls " + ClearCaseTestFixture.getTestDirPath()));
		assertFalse("cleartool should have failed", runner.runCanFail("cleartool ls " + "c:\temp"));
	}
}
