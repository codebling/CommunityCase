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

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.OutputUtil;
import net.sourceforge.clearcase.enums.Error;
import net.sourceforge.clearcase.status.ClearCaseError;
import net.sourceforge.clearcase.status.ClearCaseStatus;

public class IdenticalPredecessor extends AbstractMatcherStrategy {

	/* (non-Javadoc)
	 * @see net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy#getPattern()
	 */
	@Override
	protected String getPattern() {
		return "cleartool: Error: By default, won't create version with data identical to predecessor.";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy#getResult()
	 */
	@Override
	protected ClearCaseStatus getResult(StringBuffer output, Matcher matcher, ClearCaseStatus status) {
		// create a Pattern
		Pattern pattern = Pattern
				.compile("cleartool: Error: Unable to check in .*$");
		// Attempt to match the first candidate String
		Matcher m = pattern.matcher(output);
		//TODO:Avoid illegal state if no match is found.
		m.find();
		String element = OutputUtil.getFirstElementNameBetweenQuotation(m.group());
		status.addError(new ClearCaseError(Error.PREDECESSOR_IS_IDENTICAL, element));
		return status;
	}

}
