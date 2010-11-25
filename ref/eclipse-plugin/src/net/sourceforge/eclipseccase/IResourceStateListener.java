/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - new features, enhancements and bug fixes
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import org.eclipse.core.resources.IResource;

/**
 * A listener that will be informed if the ClearCase state of a resource
 * changes.
 */
public interface IResourceStateListener {

	/**
	 * The ClearCase state of the specified resource has changed.
	 * 
	 * @param resources
	 */
	void resourceStateChanged(IResource[] resources);
}