/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mikael Petterson
 *   Vincent Latombe
 *******************************************************************************/

package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * TODO Provide description for UnableToCreateElement.
 */
public class UnableToAccessElement extends AbstractMatcherStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getPattern()
	 */
	@Override
	protected String getPattern() {
		return "cleartool: Error: Unable to access \"(.*)\": No such file or directory\\.";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getResult(java.lang.StringBuffer, java.util.regex.Matcher)
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		String element = matcher.group(1);
		return new ClearCaseElementState(element, ClearCase.MISSING);
	}

}
