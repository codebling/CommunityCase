/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipseccase.tools;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * A simple XML writer.
 */
public class XMLWriter extends PrintWriter {

	protected int tab;

	/* constants */
	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$

	/**
	 * Creates a new instance.
	 * 
	 * @param output
	 * @throws UnsupportedEncodingException
	 */
	public XMLWriter(OutputStream output) throws UnsupportedEncodingException {
		super(new OutputStreamWriter(output, "UTF8")); //$NON-NLS-1$
		tab = 0;
		println(XML_VERSION);
	}

	/**
	 * @param name
	 */
	public void endTag(String name) {
		tab--;
		printTag('/' + name, null);
	}

	/**
	 * @param name
	 * @param value
	 */
	public void printSimpleTag(String name, Object value) {
		if (value != null) {
			printTag(name, null, true, false);
			print(getEscaped(String.valueOf(value)));
			printTag('/' + name, null, false, true);
		}
	}

	/**
	 * prints tabulation
	 */
	public void printTabulation() {
		for (int i = 0; i < tab; i++) {
			super.print('\t');
		}
	}

	@SuppressWarnings("unchecked")
	private void printTag(String name, HashMap parameters) {
		printTag(name, parameters, true, true);
	}

	@SuppressWarnings("unchecked")
	private void printTag(String name, HashMap parameters, boolean printTab,
			boolean newLine) {
		printTag(name, parameters, printTab, newLine, false);
	}

	@SuppressWarnings("unchecked")
	private void printTag(String name, HashMap parameters, boolean printTab,
			boolean newLine, boolean end) {
		StringBuffer sb = new StringBuffer();
		sb.append("<"); //$NON-NLS-1$
		sb.append(name);
		if (parameters != null) {
			for (Enumeration enumeration = Collections.enumeration(parameters
					.keySet()); enumeration.hasMoreElements();) {
				sb.append(" "); //$NON-NLS-1$
				String key = (String) enumeration.nextElement();
				sb.append(key);
				sb.append("=\""); //$NON-NLS-1$
				sb.append(getEscaped(String.valueOf(parameters.get(key))));
				sb.append("\""); //$NON-NLS-1$
			}
		}
		if (end) {
			sb.append('/');
		}
		sb.append(">"); //$NON-NLS-1$
		if (printTab) {
			printTabulation();
		}
		if (newLine) {
			println(sb.toString());
		} else {
			print(sb.toString());
		}
	}

	/**
	 * @param name
	 * @param parameters
	 */
	@SuppressWarnings("unchecked")
	public void startTag(String name, HashMap parameters) {
		startTag(name, parameters, true);
	}

	/**
	 * @param name
	 * @param parameters
	 * @param newLine
	 */
	@SuppressWarnings("unchecked")
	public void startTag(String name, HashMap parameters, boolean newLine) {
		printTag(name, parameters, true, newLine);
		tab++;
	}

	/**
	 * @param name
	 * @param parameters
	 * @param newLine
	 */
	@SuppressWarnings("unchecked")
	public void startAndEndTag(String name, HashMap parameters, boolean newLine) {
		printTag(name, parameters, true, true, true);
	}

	private static void appendEscapedChar(StringBuffer buffer, char c) {
		String replacement = getReplacement(c);
		if (replacement != null) {
			buffer.append('&');
			buffer.append(replacement);
			buffer.append(';');
		} else {
			buffer.append(c);
		}
	}

	/**
	 * @param s
	 * @return
	 */
	public static String getEscaped(String s) {
		StringBuffer result = new StringBuffer(s.length() + 10);
		for (int i = 0; i < s.length(); ++i) {
			appendEscapedChar(result, s.charAt(i));
		}
		return result.toString();
	}

	private static String getReplacement(char c) {
		// Encode special XML characters into the equivalent character
		// references.
		// These five are defined by default for all XML documents.
		switch (c) {
		case '<':
			return "lt"; //$NON-NLS-1$
		case '>':
			return "gt"; //$NON-NLS-1$
		case '"':
			return "quot"; //$NON-NLS-1$
		case '\'':
			return "apos"; //$NON-NLS-1$
		case '&':
			return "amp"; //$NON-NLS-1$
		}
		return null;
	}
}
