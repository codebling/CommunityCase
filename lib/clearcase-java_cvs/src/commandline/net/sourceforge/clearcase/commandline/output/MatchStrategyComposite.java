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
package net.sourceforge.clearcase.commandline.output;

import java.util.Enumeration;
import java.util.Vector;

import net.sourceforge.clearcase.ClearCaseCLIImpl;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * This is the composite object that passes the output from clearcase to each
 * class that implements the PatternMatcherStrategy interface.
 * 
 */
public class MatchStrategyComposite implements PatternMatcherStrategy {

	private Vector<PatternMatcherStrategy> checks = new Vector<PatternMatcherStrategy>();

	/*
	 * Check for each line of output from clearcase command.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see se.petterson.test.PatternMatcherStrategy#check(java.lang.String)
	 */
	public ClearCaseElementState check(String output) throws ClearCaseException {
		// A list of all clearcase scenrios to match against.
		Enumeration<PatternMatcherStrategy> e = checks.elements();
		while (e.hasMoreElements()) {
			PatternMatcherStrategy strategy = e.nextElement();
			// Match all patterns.
			ClearCaseElementState state = strategy.check(output);
			if (state != null) {
				if (ClearCaseCLIImpl.getDebugLevel() > 1) {
					System.out.println("MatchStrategyComposite.check() hit: "
							+ strategy.getClass().getSimpleName()); // abu
				}
				return state;
			} else {
				// if (ClearCaseCLIImpl.getDebugLevel() > 1) {
				// System.out.println("MatchStrategyComposite.check() fail: "
				// + strategy.getClass().getSimpleName()); // abu
				// }
			}
		}
		// This should occur when no strategy can match the output
		// (example: output from a trigger, not handled)
		if (ClearCaseCLIImpl.getDebugLevel() > 1) {
			System.out.println("MatchStrategyComposite.check() hit: NONE"); // abu
		}
		return null;
	}

	public void addStrategy(PatternMatcherStrategy s) {
		checks.addElement(s);
	}

}
