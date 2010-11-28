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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class providing access to messages contained in resource bundle.
 */
public class Messages {

	private static final String BUNDLE_NAME = "net.sourceforge.clearcase.messages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	/**
	 * The hidden constructor.
	 */
	private Messages() {
		// empty
	}

	/**
	 * Returns the specified string from the resource bundle.
	 * <p>
	 * The key is returned if the string is not available in the resource bundle
	 * or the resource bundle is missing.
	 * </p>
	 * 
	 * @param key
	 * @return the specified string or the key.
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the specified string formated with the given arguments.
	 * 
	 * @param key
	 * @param args
	 * @return the specified string formated with the given arguments or the key
	 * @see #getString(String)
	 * @see MessageFormat#format(java.lang.String, java.lang.Object[])
	 */
	public static String getString(String key, Object[] args) {
		return MessageFormat.format(getString(key), args);
	}

	/**
	 * Returns the specified string formated with the given argument.
	 * 
	 * @param key
	 * @param arg
	 * @return the specified string formated with the given argument or the key
	 * @see #getString(String, Object[])
	 */
	public static String getString(String key, Object arg) {
		return MessageFormat.format(getString(key), new Object[] { arg });
	}

	/**
	 * Returns the specified string formated with the given arguments.
	 * 
	 * @param key
	 * @param arg1
	 * @return the specified string formated with the given arguments or the key
	 * @see #getString(String, Object[])
	 */
	public static String getString(String key, Object arg1, Object arg2) {
		return MessageFormat
				.format(getString(key), new Object[] { arg1, arg2 });
	}
}