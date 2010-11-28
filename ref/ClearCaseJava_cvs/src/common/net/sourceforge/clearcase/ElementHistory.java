/*******************************************************************************
 * Copyright (c) 2002, 2009 eclipse-ccase.sourceforge.net team and others
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element history class used to retrieve history information
 */
public class ElementHistory {
	private String element;
	private String date;
	private String user;
	private String version;
	private String label;
	private String comment;

	/**
	 * Creates a new instance.
	 * 
	 * @param element
	 * @param date
	 * @param user
	 * @param version
	 * @param label
	 * @param comment
	 */
	public ElementHistory(String element, String date, String user,
			String version, String label, String comment) {
		super();
		this.element = element;
		this.date = date;
		this.user = user;
		this.version = version;
		this.label = label;

		Pattern commentSpace = Pattern
				.compile("^[ ]*[\"][ ]*(.*)[ ]*[\"][ ]*$");
		Matcher m;
		if ((m = commentSpace.matcher(comment)).matches()) {
			this.comment = m.group(1);
		} else {
			this.comment = comment;
		}
	}

	/**
	 * Returns the user.
	 * 
	 * @return returns the user
	 */
	public String getuser() {
		return user;
	}

	/**
	 * Returns the version.
	 * 
	 * @return returns the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Returns the label.
	 * 
	 * @return returns the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the date.
	 * 
	 * @return returns the date
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Returns the comment.
	 * 
	 * @return returns the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Returns the element.
	 * 
	 * @return returns the element
	 */
	public String getElement() {
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "{" + element + ", " + date + ", " + user + ", " + version
				+ ", " + (label != null ? "(" + label + "), " : "") + comment
				+ "}";
	}
}
