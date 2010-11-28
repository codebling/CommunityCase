/*******************************************************************************
 * Copyright (c) 2002, 2010 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * TODO Provide description for ElementIsReservedCheckout.
 */
public class ElementIsReservedCheckout extends AbstractMatcherStrategy{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.GenericStrategy
	 * #getPattern()
	 */
	@Override
	protected String getPattern() {
		return "Changed\\scheckout\\sto\\s(reserved)\\sfor\\s\\\"(.*)\\\"\\sbranch\\s\\\"(.*)\\\"";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.output.strategy.GenericStrategy
	 * #getResult()
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		return new ClearCaseElementState(matcher.group(2), ClearCase.RESERVED
				| ClearCase.IS_ELEMENT, matcher.group(3));
	}

}
