/*******************************************************************************
 * Copyright (c) 2002, 2008 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Mikael Petterson
 *******************************************************************************/
package net.sourceforge.clearcase.commandline.output;


/**
 * Static helper methods for handling output from cleartool.
 */
public final class OutputUtil {

	public static String[] getElementNameBetweenQuotation(final String output) {
		final String[] sp = output.split("\"");
		final String[] res = new String[sp.length / 2];
		for (int i = 0; i < res.length; i++) {
			res[i] = sp[2 * i + 1];
		}
		return res;
	}

	public static String getFirstElementNameBetweenQuotation(final String output) {
		final String[] sp = output.split("\"");
		if (sp.length > 0) {
			return sp[1];
		} else {
			return null;
		}
	}

	private OutputUtil() {
	}

}
