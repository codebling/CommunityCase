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
import java.util.regex.Pattern;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * TODO Provide description for MostRecentNotPredecessor.
 */
public class MostRecentNotPredecessor extends AbstractMatcherStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getPattern()
	 */
	@Override
	protected String getPattern() {
		return "cleartool: Error: The most recent version on branch .*";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getResult(java.lang.StringBuffer)
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		// create a Pattern
		Pattern pattern = Pattern
				.compile("cleartool: Error: Unable to check in .*$");
		// Attempt to match the first candidate String
		Matcher m = pattern.matcher(output);
		// TODO:Avoid illegal state if no match is found.
		m.find();

		throw new ClearCaseException(
				ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION);
	}

}
