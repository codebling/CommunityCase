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
package net.sourceforge.clearcase;

/**
 * Classes which implement this interface can be registered to a ClearCase
 * interface and will be notified about ongoing operations in the interface.
 */
public interface OperationListener {

	/**
	 * Notifies the receiver that the specified operation has started.
	 * 
	 * @param amountOfWork
	 *            the amount of expected work
	 */
	void startedOperation(int amountOfWork);

	/**
	 * Notifies the receiver about progress of the specified operation.
	 * 
	 * @param ticks
	 *            the progress
	 */
	void worked(int ticks);

	/**
	 * Notifies the receiver that an operation is still ongoing but that no
	 * progress information is available.
	 * 
	 */
	void ping();

	/**
	 * Notifies the receiver that the specified operation finished.
	 * <p>
	 * Note that the listener may not be notified if an exception occurs while
	 * performing the operation.
	 * </p>
	 */
	void finishedOperation();

	/**
	 * Notifies the caller if the operation should be canceled.
	 * 
	 * @return <code>true</code> if the caller (the ClearCase interface)
	 *         should cancel the current operation, <code>false</code>
	 *         otherwise
	 */
	boolean isCanceled();
}