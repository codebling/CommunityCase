/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - API rework, feature enhancements, bug fixes
 *******************************************************************************/
package net.sourceforge.clearcase;

/**
 * This runtime exception is thrown whenever a recoverable error occurs
 * internally in ClearCase. The message text and error code provide a further
 * description of the problem. The exception has a <code>throwable</code> field
 * which holds the underlying exception that caused the problem (if this
 * information is available (i.e. it may be null)).
 * <p>
 * ClearCaseExceptions are thrown when something fails internally, but ClearCase
 * is left in a known stable state (eg. a widget call was made from a non-u/i
 * thread, or there is failure while reading an Image because the source file
 * was corrupt).
 * </p>
 * 
 * @see net.sourceforge.clearcase.ClearCaseError
 */
public class ClearCaseException extends RuntimeException {

	/** field <code>serialVersionUID</code> */
	private static final long serialVersionUID = 6140369668072507802L;

	/** the elements that caused the error */
	private String[] elements;

	/** element states of already modified elements */
	private ClearCaseElementState[] elementStates;

	/**
	 * The ClearCase error code, one of ClearCase.ERROR_*.
	 */
	private int errorCode;

	/**
	 * Constructs a new instance of this class with its stack trace filled in.
	 * The error code is set to an unspecified value.
	 */
	public ClearCaseException() {
		this(ClearCase.ERROR_UNSPECIFIED);
	}

	/**
	 * Constructs a new instance of this class with its stack trace and error
	 * code filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 */
	public ClearCaseException(int code) {
		this(code, ClearCase.findErrorText(code));
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code
	 * and message filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param message
	 *            the detail message for the exception
	 */
	public ClearCaseException(int code, String message) {
		this(code, message, null);
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code
	 * and message and cause filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param message
	 *            the detail message for the exception
	 * @param cause
	 *            the underlying throwable that caused the problem
	 */
	public ClearCaseException(int code, String message, Throwable cause) {
		this(code, message, cause, null, null);
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code,
	 * message, cause and related elements filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param message
	 *            the detail message for the exception
	 * @param cause
	 *            the underlying throwable that caused the problem
	 * @param elements
	 *            the elements related to the error
	 * @param elementStates
	 *            the element states of already modified elements
	 */
	public ClearCaseException(int code, String message, Throwable cause,
			String[] elements, ClearCaseElementState[] elementStates) {
		super(message, cause);
		this.errorCode = code;
		this.elements = elements;
		this.elementStates = elementStates;
	}

	/**
	 * Constructs a new instance of this class with its stack trace and error
	 * code and related elements filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param elements
	 *            the elements related to the error
	 * @param elementStates
	 *            the element states of already modified elements
	 */
	public ClearCaseException(int code, String[] elements,
			ClearCaseElementState[] elementStates) {
		this(code, ClearCase.findErrorText(code), null, elements, elementStates);
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code
	 * and cause filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param cause
	 *            the underlying throwable that caused the problem
	 */
	public ClearCaseException(int code, Throwable cause) {
		this(code, ClearCase.findErrorText(code), cause);
	}

	/**
	 * Constructs a new instance of this class with its stack trace and message
	 * filled in. The error code is set to an unspecified value.
	 * 
	 * @param message
	 *            the detail message for the exception
	 */
	public ClearCaseException(String message) {
		this(ClearCase.ERROR_UNSPECIFIED, message);
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code
	 * and cause filled in.
	 * 
	 * @param cause
	 *            the underlying throwable that caused the problem
	 */
	public ClearCaseException(Throwable cause) {
		this(ClearCase.ERROR_UNSPECIFIED, cause);
	}

	/**
	 * Returns the elements that caused to the error.
	 * 
	 * @return the elements (maybe <code>null</code>)
	 */
	public String[] getElements() {
		return elements;
	}

	/**
	 * Returns the errorCode.
	 * 
	 * @return returns the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * Returns the string describing this ClearCaseException object.
	 * <p>
	 * It is combined with the message string of the Throwable which caused this
	 * ClearCaseException (if this information is available).
	 * </p>
	 * 
	 * @return the error message string of this ClearCaseException object
	 */
	@Override
	public String getMessage() {
		if (getCause() == null)
			return super.getMessage();
		else
			return super.getMessage() + " (" + getCause().toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the element states of already modified elements.
	 * 
	 * @return returns the element states (maybe <code>null</code>)
	 */
	public ClearCaseElementState[] getElementStates() {
		return elementStates;
	}
}