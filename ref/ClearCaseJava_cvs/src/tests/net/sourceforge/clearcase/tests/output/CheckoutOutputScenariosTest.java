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
 * This class tests handling of output from the checkout command from cleartool.
 */
public class CheckoutOutputScenariosTest extends AbstractOutputTestCase {

	/**
	 * Test that verifies a successful checkout.
	 */
	public void testSuccessfulCheckout() {
		String[] cleartoolOutput = new String[] { "Checked out \"M:\\eraonel_testa\\mbv_admin\\testarea\\productsample\\fi\\Finnish.java\" version \"\\main\\1\".\r\n" };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.CHECKED_OUT);
		} catch (ClearCaseException e) {
			fail();
		}
	}

	/**
	 * Test that verifies a checkout of an already checked out element.
	 */
	public void testElementAlreadyCheckedOut() {
		String[] cleartoolOutput = new String[] { "cleartool: Error: Element \"test1.txt\" is already checked out to view \"myview\"." };

		try {
			getClearCaseCLI().parserCCOutput(cleartoolOutput);
			fail();
		} catch (ClearCaseException e) {
			assertTrue(e.getErrorCode() == ClearCase.ERROR_ALREADY_CHECKED_OUT);
		}
	}

}
