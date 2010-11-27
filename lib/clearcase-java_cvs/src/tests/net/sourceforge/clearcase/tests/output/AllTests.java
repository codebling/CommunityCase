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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * TODO Provide description for AllTests.
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for net.sourceforge.clearcase.tests.output");
		// $JUnit-BEGIN$
		suite.addTestSuite(MoveOutputScenarioTest.class);
		suite.addTestSuite(UpdateOutputScenariosTest.class);
		suite.addTestSuite(AddOutputScenariosTest.class);
		suite.addTestSuite(UncheckoutOutputScenariosTest.class);
		suite.addTestSuite(ElementIsCheckedInTest.class);
		suite.addTestSuite(CheckoutOutputScenariosTest.class);
		suite.addTestSuite(DeleteOutputScenarios.class);
		suite.addTestSuite(CheckinOutputScenariosTest.class);
		suite.addTestSuite(ElementIsCheckedOutTest.class);
		suite.addTestSuite(MergeOutputScenariosTest.class);
		suite.addTestSuite(SymbolicLinkTest.class);
		// $JUnit-END$
		return suite;
	}

}
