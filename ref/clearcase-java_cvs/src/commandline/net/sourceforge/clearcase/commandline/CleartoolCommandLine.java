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
 * A small class to construct a cleartool command array.
 */
public class CleartoolCommandLine extends CommandLine {

	/**
	 * Creates a new instance.
	 * 
	 * @param baseCmd
	 *            the base command (eg. <code>ls</code>)
	 */
	public CleartoolCommandLine(String baseCmd) {
		super(baseCmd);
	}

	/**
	 * Returns the cleartool executable command.
	 * <p>
	 * The default implementations returns only "cleartool", which expects the
	 * cleartool executable to be in the system's default lookup path.
	 * </p>
	 * 
	 * @return the cleartool executable command.
	 */
	@Override
	protected String getExecutable() {
		return "cleartool"; //$NON-NLS-1$
	}

	/**
	 * Convenience method for adding the option <code>-r</code> to the command
	 * line.
	 * 
	 * @return the command line for convenience.
	 */
	public CleartoolCommandLine recursive() {
		return (CleartoolCommandLine) this.addOption("-r");
	}
}