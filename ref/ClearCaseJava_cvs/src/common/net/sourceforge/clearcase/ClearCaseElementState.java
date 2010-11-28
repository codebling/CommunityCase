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

/**
 * This class holds data about element states. All data is provided through
 * public accessible fields.
 */
public final class ClearCaseElementState {

	/** the element. */
	public final String element;

	/** the target of a link (maybe <code>null</code>). */
	public final String linkTarget;

	/** the element state. */
	public final int state;

	/** the element version (maybe <code>null</code>). */
	public final String version;

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 */
	public ClearCaseElementState(final String element, final int state) {
		this.element = element;
		this.state = state;
		this.version = null;
		this.linkTarget = null;
	}

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final int state,
			final String version) {
		this.element = element;
		this.state = state;
		this.version = version;
		this.linkTarget = null;
	}

	/**
	 * Creates a new instance.
	 * <p>
	 * <b>NOTE: </b> Although this method is exposed as API it is not expected
	 * to be called from outside the ClearCase Java API.
	 * </p>
	 * 
	 * @param element
	 * @param state
	 * @param version
	 */
	public ClearCaseElementState(final String element, final int state,
			final String version, final String linkTarget) {
		this.element = element;
		this.state = state;
		this.version = version;
		this.linkTarget = linkTarget;
	}

	/**
	 * Indicates if the element is under ClearCase control.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is under ClearCase control,
	 *         <code>false</code> otherwise
	 */
	public boolean isElement() {
		// element must be:
		// - checked in or
		// - checked out or
		// - hijacked or
		// - missing or
		// - a link
		return 0 != (state & ClearCase.IS_ELEMENT);
	}

	/**
	 * Indicates if the element is a link.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is a link, <code>false</code>
	 *         otherwise
	 */
	public boolean isLink() {
		return 0 != (state & ClearCase.LINK);
	}

	/**
	 * Indicates if the element is outside a vob.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is outside a vob,
	 *         <code>false</code> otherwise
	 */
	public boolean isOutsideVob() {
		return 0 != (state & ClearCase.OUTSIDE_VOB);
	}

	/**
	 * Indicates if the element is missing from CC.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is missing,
	 *         <code>false</code> otherwise
	 */
	public boolean isMissing() {
		return 0 != (state & ClearCase.MISSING);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuffer string = new StringBuffer(element);
		if (0 != (state & ClearCase.CHECKED_IN)) {
			string.append(" CHECKED_IN"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.CHECKED_OUT)) {
			string.append(" CHECKED_OUT"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.CHECKED_OUT_BY_DIFFERENT_USER)) {
			string.append(" CHECKED_OUT_BY_DIFFERENT_USER"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.CHECKED_OUT_IN_DIFFERENT_BRANCH)) {
			string.append(" CHECKED_OUT_IN_DIFFERENT_BRANCH"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.CHECKED_OUT_IN_DIFFERENT_VIEW)) {
			string.append(" CHECKED_OUT_IN_DIFFERENT_VIEW"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.HIJACKED)) {
			string.append(" HIJACKED"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.MISSING)) {
			string.append(" MISSING"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.OUTSIDE_VOB)) {
			string.append(" OUTSIDE_VOB"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.RESERVED)) {
			string.append(" RESERVED"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.UNRESERVED)) {
			string.append(" UNRESERVED"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.VIEW_PRIVATE)) {
			string.append(" VIEW_PRIVATE"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.DERIVED_OBJECT)) {
			string.append(" DERIVED_OBJECT"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.LINK)) {
			string.append(" LINK"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.ERROR_ALREADY_CHECKED_OUT)) {
			string.append(" ALREADY_CHECKED_OUT"); //$NON-NLS-1$
		}
		if (0 != (state & ClearCase.MOVED)) {
			string.append(" MOVED"); //$NON-NLS-1$
		}
		if (null != version) {
			string.append(" "); //$NON-NLS-1$
			string.append(version);
		}
		if (null != linkTarget) {
			string.append(" --> "); //$NON-NLS-1$
			string.append(linkTarget);
		}
		return string.toString();
	}

	/**
	 * Indicates if the element is already checked out
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is already checked out,
	 *         <code>false</code> otherwise
	 */
	public boolean isAlreadyCheckedOut() {
		return 0 != (state & ClearCase.ERROR_ALREADY_CHECKED_OUT);
	}

	/**
	 * Indicates if the element is checked out.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is checked out,
	 *         <code>false</code> otherwise
	 */
	public boolean isCheckedOut() {
		return 0 != (state & ClearCase.CHECKED_OUT);
	}

	/**
	 * Indicates if the element is checked in.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is checked in,
	 *         <code>false</code> otherwise
	 */
	public boolean isCheckedIn() {
		return 0 != (state & ClearCase.CHECKED_IN);
	}

	/**
	 * Indicates if the element is view private.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is view private,
	 *         <code>false</code> otherwise
	 */
	public boolean isViewPrivate() {
		return 0 != (state & ClearCase.VIEW_PRIVATE);
	}

	/**
	 * Indicates if the element is a derived object.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is a derived object,
	 *         <code>false</code> otherwise
	 */
	public boolean isDerivedObject() {
		return 0 != (state & ClearCase.DERIVED_OBJECT);
	}
	
	/**
	 * Indicates if the element is in a snapshot view.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is in a snapshot view,
	 *         <code>false</code> otherwise
	 */
	public boolean isInSnapShotView() {
		return 0 != (state & ClearCase.SNAPSHOT);
	}

	/**
	 * Indicates if the element is hijacked.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is hijacked, <code>false</code>
	 *         otherwise
	 */
	public boolean isHijacked() {
		return 0 != (state & ClearCase.HIJACKED);
	}

	/**
	 * Indicates if the element was moved.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @return <code>true</code> if the element is hijacked, <code>false</code>
	 *         otherwise
	 */
	public boolean isMoved() {
		return 0 != (state & ClearCase.MOVED);

	}
}
