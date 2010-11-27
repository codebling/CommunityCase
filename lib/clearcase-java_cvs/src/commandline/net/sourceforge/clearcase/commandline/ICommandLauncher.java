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

package net.sourceforge.clearcase.commandline;

import java.io.File;

import net.sourceforge.clearcase.events.OperationListener;

/**
 * Interface to execute a single cleartool command
 */
public interface ICommandLauncher {

	/**
	 * Executes the specified command.
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
	public abstract Response execute(String[] command, File workingDir,
			String[] env, OperationListener listener);

}