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

	private OutputUtil() {
	}

	private static final char QUOTATION = '"';

	public static String getElementNameBetweenQuotation(String output) {
		if (output == null) {
			output = "";
		}
		int indexFirstQuotation = output.indexOf(QUOTATION);
		int indexLastQuotation = output.indexOf(QUOTATION,
				indexFirstQuotation + 1);

		return output
				.substring(indexFirstQuotation + 1,
						(indexLastQuotation > -1 ? indexLastQuotation : output
								.length()));
	}
}
