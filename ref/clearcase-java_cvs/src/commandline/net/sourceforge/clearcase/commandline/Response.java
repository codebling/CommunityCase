/*******************************************************************************
 * Copyright (c) 2002, 2007 eclipse-ccase.sourceforge.net team and others
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
 * TODO Provide description for a Response.
 */
public class Response {

	private String[] stdOutMsg;
	private String[] stdErrMsg;

	/** the exit value (defaults to -1) */
	private int exitValue = -1;

	public Response() {
	}

	/**
	 * Returns the exitValue.
	 * 
	 * @return returns the exitValue
	 */
	public int getExitValue() {
		return exitValue;
	}

	/**
	 * Sets the value of exitValue.
	 * 
	 * @param exitValue
	 *            the exitValue to set
	 */
	public void setExitValue(int exitValue) {
		this.exitValue = exitValue;
	}

	/**
	 * Returns the stdOutMsg.
	 * 
	 * @return returns the stdOutMsg
	 */
	public String[] getStdOutMsg() {
		return stdOutMsg;
	}

	/**
	 * Sets the value of stdOutMsg.
	 * 
	 * @param stdOutMsg
	 *            the stdOutMsg to set
	 */
	public void setStdOutMsg(String[] stdOutMsg) {
		this.stdOutMsg = stdOutMsg;
	}

	/**
	 * Returns the stdErrMsg.
	 * 
	 * @return returns the stdErrMsg
	 */
	public String[] getStdErrMsg() {
		return stdErrMsg;
	}

	/**
	 * Sets the value of stdErrMsg.
	 * 
	 * @param stdErrMsg
	 *            the stdErrMsg to set
	 */
	public void setStdErrMsg(String[] stdErrMsg) {
		this.stdErrMsg = stdErrMsg;
	}

}
