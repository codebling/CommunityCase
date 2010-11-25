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
public class ViewPrivateObjectTest extends AbstractOutputTestCase {

	/**
	 * Test for view private file.
	 */
	public void testViewPrivateFile() {
		String[] cleartoolOutput = new String[] { "view private object    /path/to/directory/view_private_file.txt" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.VIEW_PRIVATE,
				cces[0].state);
		assertEquals("Wrong path for element",
				"/path/to/directory/view_private_file.txt", cces[0].element);
	}

	/**
	 * Test for view private directory.
	 */
	public void testViewPrivateDirectory() {
		String[] cleartoolOutput = new String[] { "view private object    /path/to/directory/view_private_dir" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.VIEW_PRIVATE,
				cces[0].state);
		assertEquals("Wrong path for element",
				"/path/to/directory/view_private_dir", cces[0].element);
	}

	/**
	 * Test for view private file.
	 */
	public void testViewPrivateWindowsFile() {
		String[] cleartoolOutput = new String[] { "view private object    K:\\path\\to\\directory\\view_private_file.txt" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.VIEW_PRIVATE,
				cces[0].state);
		assertEquals("Wrong path for element",
				"K:\\path\\to\\directory\\view_private_file.txt",
				cces[0].element);
	}

	private void assertCommon(ClearCaseElementState[] cces) {
		assertNotNull(cces);
		assertEquals(1, cces.length);
	}

}
