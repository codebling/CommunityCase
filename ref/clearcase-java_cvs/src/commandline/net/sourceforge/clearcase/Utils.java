/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Vincent Latombe
 *******************************************************************************/

package net.sourceforge.clearcase;

import java.util.List;

/**
 * Utility class for ClearCaseCLIImpl
 */
public final class Utils {
	private Utils() {
	}

	public static String escapeComment(String comment) {
		// the comment is not allowed to contain NEWLINE + . + NEWLINE
		comment = comment.replaceAll("\\n\\r\\.\\n\\r", "\n\r\n\r"); //$NON-NLS-1$ //$NON-NLS-2$
		comment = comment.replaceAll("\\n\\.\\n", "\n\n"); //$NON-NLS-1$ //$NON-NLS-2$
		comment = comment.replaceAll("\\r\\.\\r", "\r\r"); //$NON-NLS-1$ //$NON-NLS-2$

		// the comment is not allowed to contain CRTL+Z (^Z) (dec 26, hex 1A)
		comment = comment.replaceAll("\\x1A", " "); //$NON-NLS-1$ //$NON-NLS-2$
		return comment;
	}

	/**
	 * Convert a list of strings into an array of strings
	 * 
	 * @param list
	 * @return
	 */
	public static String[] toArray(final List<String> list) {
		return list.toArray(new String[list.size()]);
	}
}
