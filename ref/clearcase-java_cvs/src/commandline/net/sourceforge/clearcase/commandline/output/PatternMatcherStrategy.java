/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mikael Petterson
 *******************************************************************************/
package net.sourceforge.clearcase.commandline.output;

import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * All classes that checks/parses the output from clearcase should implement
 * this interface.
 */
public interface PatternMatcherStrategy {

	public ClearCaseElementState check(String output) throws ClearCaseException;

}
