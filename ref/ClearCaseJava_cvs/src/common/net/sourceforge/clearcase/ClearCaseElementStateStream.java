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

package net.sourceforge.clearcase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO Provide description for ClearCaseElementStateStream.
 */
public class ClearCaseElementStateStream {
	private List elements;
	private boolean end = false;

	@SuppressWarnings("unchecked")
	public ClearCaseElementStateStream() {
		elements = Collections.synchronizedList(new ArrayList());
	}

	public ClearCaseElementState next() {
		return (ClearCaseElementState) elements.remove(0);
	}

	public boolean hasNext() {
		return !elements.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public void add(ClearCaseElementState element) {
		elements.add(element);
		notifyAll();
	}

	public void endStream() {
		end = true;
	}

	public boolean isEnd() {
		return end;
	}

}
