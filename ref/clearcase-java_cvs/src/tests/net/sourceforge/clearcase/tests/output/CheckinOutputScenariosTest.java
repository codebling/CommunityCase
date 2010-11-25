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
 * TODO Provide description for CheckinTest.
 */
public class CheckinOutputScenariosTest extends AbstractOutputTestCase {

	/**
	 * Tests when a file is checked in and the predecessor is identical.
	 * Cleartool will generate an error message.
	 */
	public void testIdenticalResourceCheckin() {
		String[] cleartoolOutput = new String[] {
				"Jalopy Source Code Formatter 1.9_126\r\n",
				"Site License Owner: Ericsson AB, Kista, Sweden\r\n",
				"Copyright (c) 2003-2008 TRIEMAX Software Ltd. All rights reserved.\r\n",
				"[INFO] Detecting code convention format\r\n",
				"[INFO] Jalopy code convention detected\r\n",
				"[INFO] Activate profile \"wong\"\r\n",
				"[INFO] Importing settings into active profile \"wong\"\r\n",
				"[INFO] Imported 732 keys into active profile \"wong\"\r\n",
				"[INFO] M:\\eraonel_testa\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ProductC.java:0:0:File clean, skipped\r\n",
				"[INFO] 0 files formatted in 0.656 sec\r\n",
				"command exited with value 0\r\n",
				"cleartool: Error: By default, won't create version with data identical to predecessor.\r\n",
				"cleartool: Error: Unable to check in \"M:\\eraonel_testa\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ProductC.java\".\r\n" };
		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL);

		}
	}

	/**
	 * Tests for the scenario . Cleartool will generate an error message.
	 */
	public void testMostRecentNotPredecessorCheckin() {
		String[] cleartoolOutput = new String[] {
				"cleartool: Error: The most recent version on branch \"\\main\" is not the predecessor of this version.\r\n",
				"cleartool: Error: Unable to check in \"M:\\eraonel_testa\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ProductC.java\".\r\n" };
		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION);

		}
	}

	/**
	 * Test that verifies a successful checkin.
	 */
	public void testSuccessfulCheckin() {
		String[] cleartoolOutput = new String[] { "Checked in \"M:\\eraonel_testa\\mbv_admin\\testarea\\productsample\\fi\\Finnish.java\" version \"\\main\\1\".\r\n" };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.CHECKED_IN);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/**
	 * Error message: cleartool: Error: Branch "\main" of element is checked out
	 * reserved by view eraonel_testa
	 * ("ccview01.rnd.ki.sw.ericsson.se:/views04/eraonel_testa.vws"). cleartool:
	 * Error: Unable to check in"C:\cc_views\eraonel_wong\mbv_admin\testarea\productsample\com\sample\newrelease\products\TestZProduct.java"
	 * .
	 */
	public void testElementCheckedOutReservedInOtherView() {
		String[] cleartoolOutput = new String[] {
				"cleartool: Error: Branch \"\\main\" of element is checked out reserved by view eraonel_testa (\"ccview01.rnd.ki.sw.ericsson.se:/views04/eraonel_testa.vws\").\r\n",
				"cleartool: Error: Unable to check in \"C:\\cc_views\\eraonel_wong\\mbv_admin\\testarea\\productsample\\com\\sample\\newrelease\\products\\TestZProduct.java\".\r\n" };

		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS);
		}
	}
}
