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
 * TODO Provide description for UncheckoutOutputScenariosTest.
 */
public class UncheckoutOutputScenariosTest extends AbstractOutputTestCase {

	/**
	 * Test that verifies a successful uncheckout.
	 */
	public void testSuccessfulUncheckout() {
		String[] cleartoolOutput = new String[] { "Checkout cancelled for \"test1.txt\"." };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.CHECKED_IN);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/**
	 * Test that verifies a successful uncheckout.
	 */
	public void testSuccessfulUncheckoutWithLeftZeroVersionOnBranch() {
		String[] cleartoolOutput = new String[] {
				"Checkout cancelled for \"test1.txt\".\r\n",
				"cleartool: Warning: This uncheckout left only version zero on branch \"/main\".\r\n" };
		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.CHECKED_IN);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/*
	 * FIXME: This is not tested yet! cleartool: Warning: Object “conv.c” no
	 * longer referenced. cleartool: Warning: Moving object to vob lost+found
	 * directory as "conv.c.3d90000112fc11cba70e0800690605d8". Checkout
	 * cancelled for "subd".
	 */
}
