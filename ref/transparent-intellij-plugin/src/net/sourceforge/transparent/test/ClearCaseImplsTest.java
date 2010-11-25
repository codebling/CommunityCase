package net.sourceforge.transparent.test;

import junit.framework.TestCase;
import net.sourceforge.transparent.MockClearCase;
import net.sourceforge.transparent.CommandLineClearCase;
import net.sourceforge.transparent.ClearCase;
import net.sourceforge.transparent.Status;

import java.io.File;
import java.io.IOException;

public class ClearCaseImplsTest extends TestCase {
	private static final File _base = new File(".");
	private ClearCase _cc;

	public ClearCaseImplsTest(String s) {
		super(s);
	}

	protected void tearDown() throws Exception {
		cleanup(new File(_base, "foobar"));
	}

	private void cleanup(File file) {
//		if (_cc.getStatus(file).equals(Status.CHECKED_IN))
//		if (file.isDirectory()) {
//			File[] files = file.listFiles();
//			for (int i = 0; i < files.length; i++) {
//				recursiveDelete(files[i]);
//			}
//		}
//		// now we're empty
//
//		assertTrue("couldn't delete " + file, file.delete());
	}

	public void testMockClearCase() throws IOException {
		_cc = new MockClearCase();
		((MockClearCase)_cc).getElements().add(_base);

		new ClearCaseImplTester(_cc).test(_base);
	}

//	public void testCommandLineClearCase() throws Throwable {
//		_cc = new CommandLineClearCase();
//
//		new ClearCaseImplTester(_cc).test(_base);
//	}
}
