package net.sourceforge.clearcase.enums;

/**
 * This enum is used to describe ClearCase errors
 * 
 * @author Vincent
 * 
 */
public enum Error {
	/**
	 * ClearCase error indicating that no error number was specified.
	 */
	UNSPECIFIED,
	/**
	 * ClearCase error caused by an exception.
	 */
	EXCEPTION,
	/**
	 * ClearCase error indicating that a specific element is not a ClearCase
	 * element.
	 */
	NOT_AN_ELEMENT,
	/**
	 * ClearCase error indicating that a specific element is already checked
	 * out.
	 */
	ALREADY_CHECKED_OUT,
	/**
	 * ClearCase error indicating that a specific element is not checked out.
	 */
	NOT_CHECKED_OUT,
	/**
	 * ClearCase error indicating that a specific element is already added.
	 */
	ALREADY_ADDED,
	/**
	 * ClearCase error constant indicating that a null argument was passed in.
	 */
	NULL_ARGUMENT,
	/**
	 * ClearCase error constant indicating that an invalid argument was passed
	 * in
	 */
	INVALID_ARGUMENT,
	/**
	 * ClearCase error constant indicating that a particular feature has not
	 * been implemented on this platform.
	 */
	NOT_IMPLEMENTED,
	/**
	 * ClearCase error constant indicating that an unsatisfied link error
	 * occurred while attempting to load a library.
	 */
	FAILED_LOAD_LIBRARY,
	/**
	 * ClearCase error constant indicating that an input/output operation failed
	 * during the execution of a ClearCase operation.
	 */
	IO,
	/**
	 * ClearCase error constant indicating that an interface is disposed.
	 */
	INTERFACE_DISPOSED,
	/**
	 * ClearCase error constant indicating that an operation was canceled.
	 */
	OPERATION_CANCELED,
	/**
	 * ClearCase error indicating that a specific element is not accessible.
	 */
	NOT_ACCESSIBLE,
	/**
	 * ClearCase error indicating that a threads for reading i/o from clearcase
	 * was interrupted.
	 */
	THREAD_INTERRUPTED,
	/**
	 * ClearCase error indicating that you cannot modify directory since it is
	 * not checked-out.
	 */
	DIR_IS_NOT_CHECKED_OUT,
	/**
	 * ClearCase error indicating that predecessor is identical file being
	 * checked in.
	 */
	PREDECESSOR_IS_IDENTICAL,
	/**
	 * ClearCase error indicating that most recent version on branch is not the
	 * predecessor of this version.
	 */
	MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION,
	/**
	 * ClearCase error indicating that parent directory is not checked out.
	 */
	PARENT_IS_NOT_CHECKED_OUT,
	/**
	 * ClearCase error indicating that the element has checkouts in a another
	 * view.
	 */
	ELEMENT_HAS_CHECKOUTS,
	/**
	 * ClearCase error indicating that the element could not be created.
	 */
	CAN_NOT_CREATE_ELEMENT
}
