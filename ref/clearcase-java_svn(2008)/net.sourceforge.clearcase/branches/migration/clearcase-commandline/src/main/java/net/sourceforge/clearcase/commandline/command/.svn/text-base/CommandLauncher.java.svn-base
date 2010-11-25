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
package net.sourceforge.clearcase.commandline.command;

import java.io.File;
import java.io.IOException;

import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.clearcase.OperationListener;

import org.apache.commons.lang.Validate;

/**
 * This class executes a CLI command.
 */
public class CommandLauncher {

	private StreamReaderThread err;

	/** the exit value (defaults to -1) */
	private int exitValue = -1;

	private StreamReaderThread out;

	/**
	 * Creates a new instance and executes the specified command.
	 * 
	 * @param command
	 *            the command line
	 * @param workingDir
	 *            the working directory of the subprocess, or <tt>null</tt> if
	 *            the subprocess should inherit the working directory of the
	 *            current process.
	 * @param env
	 *            array of strings, each element of which has environment
	 *            variable settings in format <i>name </i>= <i>value </i>.If
	 *            <code>null</code>, the environment of the current process is
	 *            used.
	 * @param listener
	 *            an operation listener (maybe <code>null</code>) that will be
	 *            asked if the running process should be canceled
	 */

	public CommandLauncher() {

	}

	public void execute(final String[] command, final File workingDir,
			final String[] env, final OperationListener listener)
			throws ClearCaseException {
		Validate.notNull(command, "Command must not be null");

		out = new StreamReaderThread();
		err = new StreamReaderThread();
		try {
			// Start threads storing the output of the basecommand.
			final Process process = Runtime.getRuntime().exec(command, env,
					workingDir);
			// Set input streams and wakeup reader threads
			out.setInputStream(process.getInputStream());
			err.setInputStream(process.getErrorStream());
			out.start();
			err.start();

			process.waitFor();

			exitValue = process.exitValue();

		} catch (final IOException e) {
			throw new ClearCaseException(
					"IOException during the execution of the command line", e);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}

	/**
	 * Returns the error output.
	 * 
	 * @return the error output
	 */
	public String[] getError() {
		return this.err.getOutput();
	}

	/**
	 * Returns the exit value.
	 * 
	 * @return the exit value
	 */
	public int getExitValue() {
		return exitValue;
	}

	/**
	 * Returns the process output.
	 * 
	 * @return the process output
	 */
	public String[] getOutput() {
		return this.out.getOutput();
	}

}