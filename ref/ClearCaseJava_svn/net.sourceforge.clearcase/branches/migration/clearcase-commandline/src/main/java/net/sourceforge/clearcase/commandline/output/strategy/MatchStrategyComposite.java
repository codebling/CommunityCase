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

import java.util.Iterator;
import java.util.Vector;

import net.sourceforge.clearcase.commandline.output.PatternMatcherStrategy;
import net.sourceforge.clearcase.status.ClearCaseStatus;

/**
 * This is the composite object that passes the output from clearcase to each
 * class that implements the PatternMatcherStrategy interface.
 * 
 */
public class MatchStrategyComposite implements PatternMatcherStrategy {

	private Vector<PatternMatcherStrategy> checks = new Vector<PatternMatcherStrategy>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.petterson.test.PatternMatcherStrategy#check(java.lang.String)
	 */
	public ClearCaseStatus check(StringBuffer output, ClearCaseStatus status) {
		if (status == null)
			status = new ClearCaseStatus();
		for(Iterator<PatternMatcherStrategy> it = checks.iterator(); it.hasNext();){
			PatternMatcherStrategy strategy = it.next();
			strategy.check(output, status);
		}
		// This should occur when no strategy can match the output
		// (example: output from a trigger, not handled)
		return null;
	}

	public void addStrategy(PatternMatcherStrategy s) {
		checks.addElement(s);
	}

}
