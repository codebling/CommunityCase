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
 * This error is thrown whenever an unrecoverable error occurs internally in
 * ClearCase. The message text and error code provide a further description of
 * the problem.
 * <p>
 * ClearCaseErrors are thrown when something fails internally which either
 * leaves ClearCase in an unknown state (eg. the o/s call to remove an item from
 * a list returns an error code) or when ClearCase is left in a
 * known-to-be-unrecoverable state (eg. it runs out of callback resources).
 * ClearCaseErrors should not occur in typical programs, although "high
 * reliability" applications should still catch them.
 * </p>
 * <p>
 * This class also provides support methods used by ClearCase to match error
 * codes to the appropriate exception class (ClearCaseError, ClearCaseException,
 * or IllegalArgumentException) and to provide human readable strings for
 * ClearCase error codes.
 * </p>
 * 
 * @see net.sourceforge.clearcase.ClearCaseException
 * @see net.sourceforge.clearcase.ClearCase#error(int)
 */

public class ClearCaseError extends Error {

	/** field <code>serialVersionUID</code> */
	private static final long serialVersionUID = -7740045671229041261L;

	/**
	 * The ClearCase error code, one of ClearCase.ERROR_*.
	 */
	private int errorCode;

	/**
	 * Constructs a new instance of this class with its stack trace filled in.
	 * The error code is set to an unspecified value.
	 */
	public ClearCaseError() {
		this(ClearCase.ERROR_UNSPECIFIED);
	}

	/**
	 * Constructs a new instance of this class with its stack trace and message
	 * filled in. The error code is set to an unspecified value.
	 * 
	 * @param message
	 *            the detail message for the exception
	 */
	public ClearCaseError(String message) {
		this(ClearCase.ERROR_UNSPECIFIED, message);
	}

	/**
	 * Constructs a new instance of this class with its stack trace, error code
	 * and cause filled in.
	 * 
	 * @param cause
	 *            the underlying throwable that caused the problem
	 */
	public ClearCaseError(Throwable cause) {
		this(ClearCase.ERROR_UNSPECIFIED, cause);
	}

	/**
	 * Constructs a new instance of this class with its stack trace and error
	 * code filled in.
	 * 
	 * @param code
	 *            the ClearCase error code
	 */
	public ClearCaseError(int code) {
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
	public ClearCaseError(int code, String message) {
		this(code, message, null);
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
	public ClearCaseError(int code, Throwable cause) {
		this(code, ClearCase.findErrorText(code), cause);
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
	public ClearCaseError(int code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
	}

	/**
	 * Returns the string describing this ClearCaseError object.
	 * <p>
	 * It is combined with the message string of the Throwable which caused this
	 * ClearCaseError (if this information is available).
	 * </p>
	 * 
	 * @return the error message string of this ClearCaseError object
	 */
	@Override
	public String getMessage() {
		if (getCause() == null)
			return super.getMessage();
		else
			return super.getMessage() + " (" + getCause().toString() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the errorCode.
	 * 
	 * @return returns the errorCode
	 */
	public int getErrorCode() {
		return errorCode;
	}
}