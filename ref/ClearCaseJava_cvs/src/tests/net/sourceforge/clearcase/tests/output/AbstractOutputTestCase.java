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

import junit.framework.TestCase;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseCLIImpl;

/**
 * TODO Provide description for AbstractOutputTestCase.
 */
public abstract class AbstractOutputTestCase extends TestCase {
	/* the ClearCase interface */
	private ClearCaseCLIImpl ccCli;

	protected ClearCaseCLIImpl getClearCaseCLI() {
		return ccCli;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		ccCli = (ClearCaseCLIImpl) ClearCase
				.createInterface(ClearCase.INTERFACE_CLI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		ccCli.dispose();
		ccCli = null;
	}
}
