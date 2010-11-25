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

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.OutputUtil;
import net.sourceforge.clearcase.enums.Error;
import net.sourceforge.clearcase.status.ClearCaseError;
import net.sourceforge.clearcase.status.ClearCaseStatus;

/**
 * TODO Provide description for ElementCheckedOutReservedInOtherView.
 */
public class ElementCheckedOutReservedInOtherView extends AbstractMatcherStrategy {

	/* (non-Javadoc)
	 * @see net.sourceforge.clearcase.commandline.output.strategy.GenericStrategy#getPattern()
	 */
	@Override
	protected String getPattern() {
		return "cleartool: Error: Branch \"(.*)\" of element is checked out reserved by view .*";
	}

	/* (non-Javadoc)
	 * @see net.sourceforge.clearcase.commandline.output.strategy.GenericStrategy#getResult()
	 */
	@Override
	protected ClearCaseStatus getResult(StringBuffer output, Matcher matcher, ClearCaseStatus status) {
		String element = OutputUtil.getFirstElementNameBetweenQuotation(matcher.group());
		status.addError(new ClearCaseError(Error.ELEMENT_HAS_CHECKOUTS, element));
		return status;
	}

}
