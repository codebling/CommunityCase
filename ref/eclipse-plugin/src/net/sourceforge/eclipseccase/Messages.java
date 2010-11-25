/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class for accessing the resource bundle with message strings.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class Messages {

	private static final String BUNDLE_NAME = "net.sourceforge.eclipseccase.messages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	private Messages() {
		// hidden
	}

	/**
	 * Returns the string for the specified key.
	 * 
	 * @param key
	 * @return the string for the specified key
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}