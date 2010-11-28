/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.clearcase.tests.output;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * TODO Provide description for AddOutputScenariosTest.
 */
public class AddOutputScenariosTest extends AbstractOutputTestCase {

	/**
	 * Test that verifies a successful add.
	 */
	public void testSuccessfulAddFile() {
		String[] cleartoolOutput = new String[] { "Created element \"test1.txt\" (type \"text_file\").\r\n" };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.IS_ELEMENT);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/**
	 * Test that verifies that an element is already added.
	 */
	public void testEntryAlreadyExists() {
		String[] cleartoolOutput = new String[] {
				"cleartool: Error: Entry named \"test1.txt\" already exists.\r\n",
				"cleartool: Error: Unable to create element \"test1.txt\"." };

		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_ALREADY_EXISTS);
		}
	}

	/**
	 * Test that verifies that parent directory is not checkedout.
	 */
	public void testDirectoryIsNotCheckedOut() {
		String[] cleartoolOutput = new String[] { "cleartool: Error: Can't modify directory \".\" because it is not checked out.\r\n" };

		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_DIR_IS_NOT_CHECKED_OUT);
		}
	}

}
