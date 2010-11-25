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
 * TODO Provide description for MoveOutputScenarioTest.
 */
public class MoveOutputScenarioTest extends AbstractOutputTestCase {

	/**
	 * TODO: Test that verifies a successful add.
	 */
	public void testSuccessfulMove() {
		String[] cleartoolOutput = new String[] {
				"cleartool: Warning: Moved element with checkouts to \"src/cm_add.c\";",
				"view private data may need to be moved.",
				"Moved \"cm_add.c\" to \"src/cm_add.c\".\r\n" };

		try {
			ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
					cleartoolOutput);
			assertNotNull("State output should not be null", cces);
			assertEquals("Expected 1 element in states", 1, cces.length);
			assertTrue(cces[0].state == ClearCase.MOVED);
			assertEquals("cm_add.c", cces[0].element);
		} catch (ClearCaseException e) {
			fail();
		}
	}

}
