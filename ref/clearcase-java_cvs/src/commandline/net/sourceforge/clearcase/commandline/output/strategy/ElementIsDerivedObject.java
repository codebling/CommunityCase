/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Achim Bursian - New strategy
 *******************************************************************************/
package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * Check cleartool output for derived object
 * 
 * @author Achim Bursian
 */
public class ElementIsDerivedObject extends AbstractMatcherStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPattern() {
		return "^derived object (\\([^\\)]*\\))?\\s*(.*)@@.*$";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		String element = matcher.group(2);
		return new ClearCaseElementState(element, ClearCase.DERIVED_OBJECT | ClearCase.IS_ELEMENT);
	}

}
