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
public class ElementIsCheckedInTest extends AbstractOutputTestCase {

	/**
	 * Test for checked in directory on a label.
	 */
	public void testDirectoryCheckedInOnLabel() {
		String[] cleartoolOutput = new String[] { "directory version      /path/to/directory@@/main/branch/1     Rule: element /path/to/directory/... LABEL_NAME -nocheckout" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_IN
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/1",
				cces[0].version);
	}

	/**
	 * Test for checked in directory on a label.
	 */
	public void testFileCheckedInOnLabel() {
		String[] cleartoolOutput = new String[] { "version      /path/to/directory/file1.txt@@/main/branch/2     Rule: element /path/to/directory/... LABEL_NAME -nocheckout" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_IN
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory/file1.txt",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/2",
				cces[0].version);
	}

	/**
	 * Test for checked in directory on a branch.
	 */
	public void testDirectoryCheckedInOnBranch() {
		String[] cleartoolOutput = new String[] { "directory version      /path/to/directory@@/main/branch/1     Rule: element /path/to/... .../directory/LATEST" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_IN
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/1",
				cces[0].version);
	}

	/**
	 * Test for checked in directory on a branch.
	 */
	public void testFileCheckedInOnBranch() {
		String[] cleartoolOutput = new String[] { "version      /path/to/directory/file1.txt@@/main/branch/2     Rule: element /path/to/... .../directory/LATEST" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.CHECKED_IN
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong path for element", "/path/to/directory/file1.txt",
				cces[0].element);
		assertEquals("Wrong version for element", "/main/branch/2",
				cces[0].version);
	}

	private void assertCommon(ClearCaseElementState[] cces) {
		assertNotNull(cces);
		assertEquals(1, cces.length);
	}

}
