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
 * TODO Provide description for DeleteOutputScenarios.
 */
public class DeleteOutputScenarios extends AbstractOutputTestCase {

	/**
	 * Test that verifies a successful add.
	 */
	public void testSuccessfulDelete() {
		String[] cleartoolOutput = new String[] {
				"cleartool: Warning: Object \"test1.txt\" no longer referenced.\r\n",
				"cleartool: Warning: Moving object to vob lost+found directory as \"test1.txt.5b5628b05b0611dc9b46001321f229d1\".\r\n",
				"Removed \"test1.txt\"." };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.REMOVED);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/**
	 * Test that verifies that there are views with checkouts of the same
	 * element.
	 */
	public void testElementHasCheckOuts() {
		String[] cleartoolOutput = new String[] { "cleartool: Error: Element \"myfile.txt\" has checkouts." };

		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS);
		}
	}

}
