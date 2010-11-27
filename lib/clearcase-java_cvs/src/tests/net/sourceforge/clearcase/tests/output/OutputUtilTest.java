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

import junit.framework.TestCase;

import net.sourceforge.clearcase.commandline.output.OutputUtil;

/**
 * Tests functionality in {@link OutputUtil}
 */
public class OutputUtilTest extends TestCase {

	public void testValidQuotation() {
		String result = OutputUtil
				.getElementNameBetweenQuotation("This is a \"test\"");
		assertNotNull(result);
		assertEquals("test", result);
	}

	public void testEmptyString() {
		String result = OutputUtil.getElementNameBetweenQuotation("");
		assertNotNull(result);
		assertEquals("", result);
	}

	public void testNullString() {
		String result = OutputUtil.getElementNameBetweenQuotation(null);
		assertNotNull(result);
		assertEquals("", result);
	}

	public void testStringWithoutQuotations() {
		String result = OutputUtil
				.getElementNameBetweenQuotation("This is a test");
		assertNotNull(result);
		assertEquals("This is a test", result);
	}

	public void testMultipleQuotations() {
		String result = OutputUtil
				.getElementNameBetweenQuotation("Checked out \"K:\\path\\to\\file\\checked_out.txt\" from version \"\\some\\element\\3\".");
		assertNotNull(result);
		assertEquals("K:\\path\\to\\file\\checked_out.txt", result);
	}
}
