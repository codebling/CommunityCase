/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vincent Latombe
 *******************************************************************************/

package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.clearcase.commandline.output.PatternMatcherStrategy;

/**
 * Provides a default behaviour to a pattern matcher strategy.
 */
public abstract class AbstractMatcherStrategy implements PatternMatcherStrategy {
	public ClearCaseElementState check(String output) throws ClearCaseException {
		// create a Pattern
		Pattern p = Pattern.compile(getPattern());
		// Attempt to match the first candidate String
		Matcher matcher = p.matcher(output);
		// true then this is the class to handle it.
		if (matcher.find())
			return getResult(output, matcher);
		else
			return null;
	}

	/**
	 * This method must be implemented to provide a pattern to match
	 * 
	 * @return
	 */
	protected abstract String getPattern();

	/**
	 * This method is called if the pattern defined in the class can be found in
	 * the output. It returns the expected result for the check public method
	 * 
	 * @param output
	 * @param matcher
	 * @return
	 * @throws ClearCaseException
	 */
	protected abstract ClearCaseElementState getResult(String output,
			Matcher matcher) throws ClearCaseException;
}
