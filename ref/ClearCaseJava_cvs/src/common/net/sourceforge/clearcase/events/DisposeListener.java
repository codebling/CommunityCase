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
package net.sourceforge.clearcase.events;

import java.util.EventListener;

import net.sourceforge.clearcase.ClearCaseInterface;

/**
 * Classes which implement this interface provide a method that deals with the
 * event that is generated when a ClearCase interface is disposed.
 * <p>
 * After creating an instance of a class that implements this interface it can
 * be added to an interface using the <code>addDisposeListener</code> method and
 * removed using the <code>removeDisposeListener</code> method. When an
 * interface is disposed, the interfaceDisposed method will be invoked.
 * </p>
 */
public interface DisposeListener extends EventListener {

	/**
	 * Sent when the ClearCase interface is disposed.
	 * 
	 * @param clearCaseInterface
	 *            an event containing information about the dispose
	 */
	public void interfaceDisposed(ClearCaseInterface clearCaseInterface);
}