/*******************************************************************************
 * Copyright (c) 2002, 2010 eclipse-ccase.sourceforge.net team and others
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

/**
 * Tests the clearcase symbolic link element.
 */
public class SymbolicLinkTest extends AbstractOutputTestCase {

	public void testSymbolicLinkVerify() {
		String[] cleartoolOutput = new String[] { "symbolic link         messages.c --> msg.c" };

		ClearCaseElementState[] cces = getClearCaseCLI().parserCCOutput(
				cleartoolOutput);

		assertCommon(cces);
		assertEquals("Wrong state for element", ClearCase.LINK
				| ClearCase.IS_ELEMENT, cces[0].state);
		assertEquals("Wrong name for element", "messages.c", cces[0].element);
		assertEquals("Wrong name for link target", "msg.c", cces[0].linkTarget);
	}

	private void assertCommon(ClearCaseElementState[] cces) {
		assertNotNull(cces);
		assertEquals(1, cces.length);
	}

}
