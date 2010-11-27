/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tobias Sodergren - initial API and implementation
 *******************************************************************************/

package net.sourceforge.clearcase.tests.output;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;

/**
 * Test case for checked in elements
 */
public class ElementIsCheckedOutTest extends AbstractOutputTestCase {

	/**
	 * Test for checked out directory.
	 */
	public void testDirectoryCheckedOutOnBranch() {
		String[] cleartoolOutput = new String[] { "directory version      /path/to/directory@@/main/branch/CHECKEDOUT from /main/branch/1                 Rule: CHECKEDOUT" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_OUT
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/1",
				cces[0].version);
	}

	/**
	 * Test for checked in directory on a label.
	 */
	public void testFileCheckedOutOnBranch() {
		String[] cleartoolOutput = new String[] { "version      /path/to/directory/file1.txt@@/main/branch/CHECKEDOUT from /main/branch/2   Rule: CHECKEDOUT" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_OUT
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory/file1.txt",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/2",
				cces[0].version);
	}

	public void testWindowsFileCheckedOutOnBranch() {
		String[] cleartoolOutput = new String[] { "version                K:\\test_dynamic_view\\VOB\\project\\plugins\\plugin.test\\src-itest\\my\\company\\path\\to\\code\\IntegrationTest.java@@\\main\\project\\component\\branch\\CHECKEDOUT from \\main\\project\\component\\branch\\3     Rule: element \\VOB\\project\\... CHECKEDOUT" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_OUT
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals(
				"Wrong path for element",
				"K:\\test_dynamic_view\\VOB\\project\\plugins\\plugin.test\\src-itest\\my\\company\\path\\to\\code\\IntegrationTest.java",
				cces[0].element);
		assertEquals("Wrong version for element",
				"\\main\\project\\component\\branch\\3", cces[0].version);
	}

	private void assertCommon(ClearCaseElementState[] cces) {
		assertNotNull(cces);
		assertEquals(1, cces.length);
	}

}
