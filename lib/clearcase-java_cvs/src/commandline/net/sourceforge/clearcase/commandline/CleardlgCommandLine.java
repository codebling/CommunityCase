/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.clearcase.commandline;

/**
 * A small class to construct a cleardlg command array.
 */
public class CleardlgCommandLine extends CommandLine {

	/**
	 * Creates a new instance.
	 * 
	 * @param baseCmd
	 *            the base command (eg. <code>/checkin</code>)
	 */
	public CleardlgCommandLine(String baseCmd) {
		super(baseCmd);
	}

	/**
	 * Returns the cleardlg executable command.
	 * <p>
	 * The default implementations returns only "cleardlg", which expects the
	 * cleardlg executable to be in the system's default lookup path.
	 * </p>
	 * 
	 * @return the cleartool executable command.
	 */
	@Override
	protected String getExecutable() {
		return "cleardlg"; //$NON-NLS-1$
	}
}