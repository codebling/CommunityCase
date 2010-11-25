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

import net.sourceforge.clearcase.ClearCase;

/**
 * A tool for building command line arrays with multiple arguments and file
 * elements.
 */
public abstract class CommandLine {

	/** the base command */
	private String baseCommand;

	/** the options */
	private String[] options;

	/** the elements */
	private String[] elements;

	/** A combination of elements and options with order. */
	private String[] orderedCmd;

	/** Flag that is set if elements and options use order. */
	private boolean ordered = false;

	/**
	 * Creates a new instance.
	 * 
	 * @param baseCmd
	 *            the base command (eg. <code>ls</code>)
	 */
	public CommandLine(String baseCmd) {
		this.baseCommand = baseCmd;
	}

	/**
	 * Adds an option.
	 * 
	 * @param option
	 * @return this instance for convenience
	 */
	public CommandLine addOption(String option) {
		if (null == options) {
			options = new String[] { option };
			return this;
		}
		String[] newOptions = new String[options.length + 1];
		System.arraycopy(options, 0, newOptions, 0, options.length);
		newOptions[options.length] = option;
		options = newOptions;
		return this;
	}

	/**
	 * Adds an element.
	 * 
	 * @param element
	 * @return this instance for convenience
	 */
	public CommandLine addElement(String element) {
		if (null == elements) {
			elements = new String[] { element };
			return this;
		}
		String[] newElements = new String[elements.length + 1];
		System.arraycopy(elements, 0, newElements, 0, elements.length);
		newElements[elements.length] = element;
		elements = newElements;
		return this;
	}

	/**
	 * Sets the elements (will overwrite any previous elements).
	 * 
	 * @param newElements
	 * @return this instance for convenience
	 */
	public CommandLine setElements(String[] newElements) {
		this.elements = newElements;
		return this;
	}

	/**
	 * Builds the command array.
	 * 
	 * @return the command array
	 */
	public String[] create() {

		// detect size
		int size = 2;
		if (options != null) {
			size += options.length;
		}
		if (elements != null) {
			size += elements.length;
		}

		// create
		String[] cmd = new String[size];

		// cleartool executable
		cmd[0] = getExecutable();
		if (null == cmd[0]) {
			ClearCase.error(ClearCase.ERROR_IO, "cleartool exectuable not set"); //$NON-NLS-1$
		}

		// base command
		cmd[1] = baseCommand;

		// options
		if (options != null) {
			System.arraycopy(options, 0, cmd, 2, options.length);
		}

		// elements
		if (elements != null) {
			System.arraycopy(elements, 0, cmd, size - elements.length,
					elements.length);
		}

		return cmd;
	}

	/**
	 * Method creates adds cleartool executable before main command and options
	 * e. g. 'merge -graphical -to opt.c -insert -version \main\r1_fix\4' after
	 * we have: 'cleartool merge -graphical -to opt.c -insert -version
	 * \main\r1_fix\4'
	 * 
	 * @return array of commands.
	 */
	public String[] createWithCmdOrder() {
		// detect size
		int size = 2;
		if (null != orderedCmd) {
			size += orderedCmd.length;
		}
		// create
		String[] cmd = new String[size];
		// cleartool executable
		cmd[0] = getExecutable();
		if (null == cmd[0]) {
			ClearCase.error(ClearCase.ERROR_IO, "cleartool exectuable not set"); //$NON-NLS-1$
		}

		// base command
		cmd[1] = baseCommand;

		// add the full command line after the cleartool executable.
		System.arraycopy(orderedCmd, 0, cmd, 2, orderedCmd.length);

		return cmd;
	}

	/**
	 * If no elements then a new array is created with this first element. If
	 * there is an array the next element will be added after.
	 * 
	 * @param element
	 * @return an CommandLine object.
	 */
	public CommandLine addElementWithOrder(String element) {
		if (null == orderedCmd) {
			orderedCmd = new String[] { element };
			ordered = true;
			return this;
		}

		String[] newOrderedCmd = new String[orderedCmd.length + 1];
		System.arraycopy(orderedCmd, 0, newOrderedCmd, 0, orderedCmd.length);
		newOrderedCmd[orderedCmd.length] = element;
		orderedCmd = newOrderedCmd;
		return this;
	}

	/**
	 * Same as addElementWithOrder but for options. It will add the option in
	 * the same array an be used as a cli command.
	 * 
	 * @param elementan
	 *            CommandLine object.
	 * @return an CommandLine object.
	 */
	public CommandLine addOptionWithOrder(String element) {
		return addElementWithOrder(element);
	}

	/**
	 * Check if the following command line object contains a list of commands
	 * that must be in a specific order.
	 * 
	 * @return true is ordered command sequence is used.
	 */
	public boolean isOrdered() {
		return ordered;
	}

	/**
	 * Returns the executable command.
	 * <p>
	 * Subclasses must implement and return the name of an executable that will
	 * be the first command in the command line array.
	 * </p>
	 * 
	 * @return the executable command.
	 */
	protected abstract String getExecutable();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String[] cmd = create();
		StringBuffer string = new StringBuffer();
		for (int i = 0; i < cmd.length; i++) {
			if (i > 0) {
				string.append(" "); //$NON-NLS-1$
			}
			string.append(cmd[i]);
		}
		return string.toString();
	}

}