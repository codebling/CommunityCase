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

import java.util.EnumSet;
import java.util.regex.Matcher;

import net.sourceforge.clearcase.commandline.output.AbstractMatcherStrategy;
import net.sourceforge.clearcase.commandline.output.OutputUtil;
import net.sourceforge.clearcase.enums.ElementStatus;
import net.sourceforge.clearcase.status.ClearCaseElementState;
import net.sourceforge.clearcase.status.ClearCaseStatus;

/**
 * TODO Provide description for SuccessfulAddElement.
 */
public class SuccessfulAdd extends AbstractMatcherStrategy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getPattern()
	 */
	@Override
	protected String getPattern() {
		return "Created element .*$";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.AbstractStrategy
	 * #getResult(java.lang.StringBuffer)
	 */
	@Override
	protected ClearCaseStatus getResult(StringBuffer output, Matcher matcher,
			ClearCaseStatus status) {
		String element = OutputUtil.getFirstElementNameBetweenQuotation(matcher
				.group());
		status.addState(new ClearCaseElementState(element, EnumSet
				.of(ElementStatus.IS_ELEMENT)));
		return status;
	}

}
