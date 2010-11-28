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

package net.sourceforge.clearcase.console;

import net.sourceforge.clearcase.events.OperationListener;

/**
 * TODO Provide description for OperationListenerImpl.
 */
public class OperationListenerImpl implements OperationListener {
	private int totalWork = 0;
	private int workDoneSoFar = 0;
	private boolean cancelRequested = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.events.OperationListener#finishedOperation()
	 */
	public void finishedOperation() {
		int workRemaining = totalWork - workDoneSoFar;
		worked(workRemaining);
		reportMsg("Operation done!");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.events.OperationListener#isCanceled()
	 */
	public boolean isCanceled() {
		// reportMsg("Cancel");
		return cancelRequested;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.events.OperationListener#ping()
	 */
	public void ping() {
		reportMsg("Operation still working ...");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.events.OperationListener#startedOperation(int)
	 */
	public void startedOperation(int amountOfWork) {
		totalWork = amountOfWork;
		reportMsg("Started ...");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.events.OperationListener#worked(int)
	 */
	public void worked(int work) {
		workDoneSoFar += work;
		reportMsg(String.valueOf(workDoneSoFar));

	}

	/**
	 * Command execution message received, print it.
	 * 
	 * @param msg
	 *            the message to print.
	 */
	public void print(String msg) {
		reportMsg("Info: " + msg);
	}

	/**
	 * Command execution error message received, print it.
	 * 
	 * @param msg
	 *            the error message to print.
	 */
	public void printErr(String msg) {
		reportMsg("Error: " + msg);
	}

	/*
	 * Report msg to console.
	 */
	public void reportMsg(String msg) {
		System.out.println(msg);
	}

	/**
	 * Operation information message
	 * 
	 * @param msg
	 *            the information message to print.
	 */
	public void printInfo(String msg) {
		System.out.println(msg);
	}
}
