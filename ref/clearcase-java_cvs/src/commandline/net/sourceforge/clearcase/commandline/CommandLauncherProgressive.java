/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import net.sourceforge.clearcase.ClearCase;

/**
 * TODO Provide description for CommandLauncherProgressive.
 */
public class CommandLauncherProgressive {
	protected InputStream out;
	protected InputStream err;
	protected Process process;

	public void execute(String[] command, File workingDir, String[] env) {

		if (null == command)
			throw new IllegalArgumentException("Command must not be null"); //$NON-NLS-1$
		try {
			ProcessBuilder pb = new ProcessBuilder(command)
					.redirectErrorStream(true).directory(workingDir);
			Map<String, String> e = pb.environment();
			e.clear();
			for (int i = 0; i < env.length; i++) {
				String[] el = env[i].split("=");
				if (el.length == 2) {
					e.put(el[0], el[1]);
				}
			}
			process = pb.start();
			// Set input streams and wake up reader threads
			out = process.getInputStream();
			err = process.getErrorStream();
		} catch (IOException e) {
			ClearCase.error(ClearCase.ERROR_IO, e, null);
		} finally {
		}
	}

	public void interrupt() {
		process.destroy();
		process = null;
	}

	public InputStream getOut() {
		return out;
	}

	public InputStream getErr() {
		return err;
	}
}
