package net.sourceforge.clearcase;


/**
 * This exception is thrown whenever a recoverable error occurs
 * internally in ClearCase. The message text and error code provide a further
 * description of the problem. The exception has a <code>throwable</code>
 * field which holds the underlying exception that caused the problem (if this
 * information is available (i.e. it may be null)).
 * <p>
 * ClearCaseExceptions are thrown when something fails internally, but ClearCase
 * is left in a known stable state (eg. a widget call was made from a non-u/i
 * thread, or there is failure while reading an Image because the source file
 * was corrupt).
 * </p>
 * 
 * @see net.sourceforge.clearcase.status.ClearCaseError
 */
public class ClearCaseException extends Exception {

	private static final long serialVersionUID = -2975504516372588455L;

	public ClearCaseException() {
		super();
	}

	public ClearCaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClearCaseException(String message) {
		super(message);
	}

	public ClearCaseException(Throwable cause) {
		super(cause);
	}
}