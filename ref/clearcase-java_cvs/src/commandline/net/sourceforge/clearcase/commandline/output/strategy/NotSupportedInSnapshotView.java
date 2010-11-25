/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Tobias Sodergren - New strategy
 *******************************************************************************/
package net.sourceforge.clearcase.commandline.output.strategy;

import java.util.regex.Matcher;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * Strategy that finds problem with command not supported in snapshot view.
 */
public class NotSupportedInSnapshotView extends AbstractMatcherStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPattern() {
		return "cleartool: Error: This command is not supported in a snapshot view.";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ClearCaseElementState getResult(String output, Matcher matcher)
			throws ClearCaseException {
		throw new ClearCaseException(ClearCase.ERROR_OPERATION_NOT_SUPPORTED,
				"Operation is not supported in snapshot view.");
	}

}
