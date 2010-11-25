/*******************************************************************************
 * Copyright (c) 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Vincent Latombe - refactor
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.clearcase.commandline.command;

import java.util.ArrayList;
import java.util.List;

/**
 * A tool for building command line arrays with multiple arguments and file
 * elements.
 */
public abstract class CommandLine {

	/** the base command */
	private String baseCommand;

	/** the elements */
	private final List<String> elements = new ArrayList<String>();

	private String executable;

	/** the options */
	private final List<String> options = new ArrayList<String>();

	/**
	 * Creates a new instance.
	 * 
	 * @param baseCmd
	 *            the base command (eg. <code>ls</code>)
	 */
	public CommandLine() {
	}

	/**
	 * Adds an element.
	 * 
	 * @param element
	 * @return this instance for convenience
	 */
	public CommandLine addElement(final String element) {
		elements.add(element);
		return this;
	}

	/**
	 * Adds an option.
	 * 
	 * @param option
	 * @return this instance for convenience
	 */
	public CommandLine addOption(final String option) {
		options.add(option);
		return this;
	}

	/**
	 * Builds the command array.
	 * 
	 * @return the command array
	 */
	public String[] create() {

		// detect size
		final int size = 2 + options.size() + elements.size();

		// create
		final String[] cmd = new String[size];

		// cleartool executable
		cmd[0] = getExecutable();
		if (cmd[0] == null) {
			throw new IllegalArgumentException(
					"Any implementation of getExecutable must return a valid non-null string");
		}

		// base command
		cmd[1] = baseCommand;

		// options
		int index = 2;
		for (final String string : options) {
			cmd[index++] = string;
		}

		// elements
		for (final String string : elements) {
			cmd[index++] = string;
		}

		return cmd;
	}

	protected String getBaseCommand() {
		return baseCommand;
	}

	protected List<String> getElements() {
		return elements;
	}

	protected String getExecutable() {
		return executable;
	}

	protected List<String> getOptions() {
		return options;
	}

	protected void setBaseCommand(final String baseCommand) {
		this.baseCommand = baseCommand;
	}

	public void setExecutable(final String executable) {
		this.executable = executable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final String[] cmd = create();
		final StringBuffer string = new StringBuffer();
		for (int i = 0; i < cmd.length; i++) {
			if (i > 0) {
				string.append(" "); //$NON-NLS-1$
			}
			string.append(cmd[i]);
		}
		return string.toString();
	}

}