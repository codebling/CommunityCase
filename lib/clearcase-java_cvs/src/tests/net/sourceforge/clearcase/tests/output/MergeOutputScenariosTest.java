/*******************************************************************************
 * Copyright (c) 2002, 2009 eclipse-ccase.sourceforge.net team and others
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
 * TODO Provide description for MergeOutputScenariosTest.
 */
public class MergeOutputScenariosTest extends AbstractOutputTestCase {

	public void testMergeOk() {
		String[] cleartoolOutput = new String[] {
				"Moved contributor \"C:\\cc_views\\wong\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ASample.java\" to \"C:\\cc_views\\wong\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ASample.java.contrib\".\r\n",
				"Output of merge is in \"C:\\cc_views\\wong\\mbv_admin\\testarea\\productsample\\com\\sample\\product\\ASample.java\"." };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertTrue(cces[0].state == ClearCase.MERGED);
		} catch (ClearCaseException e) {
			fail();
		}

	}
}
