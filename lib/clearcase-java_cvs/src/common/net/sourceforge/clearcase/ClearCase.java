/******************************************************************************
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
 ******************************************************************************/
package net.sourceforge.clearcase;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The central class for access to the ClearCase Java API.
 * <p>
 * This class cannot be instantiated; all functionality is provided by static
 * methods.
 * </p>
 * <p>
 * Features provided:
 * <ul>
 * <li>creation of the ClearCase layers.</li>
 * <li>access to the ClearCase layers.</li>
 * </ul>
 * </p>
 * <p>
 * All logging in the ClearCase Java API is done via the Java logging framework
 * (<code>java.util.logging.*</code>).
 * </p>
 */
public final class ClearCase {

	/**
	 * Indicates that a default should be used (value is -1).
	 */
	public static final int DEFAULT = -1;

	/**
	 * ClearCase error constant indicating that no error number was specified
	 * (value is 0).
	 */
	public static final int ERROR_UNSPECIFIED = 0;

	/**
	 * ClearCase error caused by an exception (value is 1).
	 */
	public static final int ERROR_EXCEPTION = 1;

	/**
	 * ClearCase error indicating that a specific element is not a ClearCase
	 * element. (value is 2).
	 */
	public static final int ERROR_NOT_AN_ELEMENT = 2;

	/**
	 * ClearCase error indicating that a specific element is already checked
	 * out. (value is 3).
	 */
	public static final int ERROR_ALREADY_CHECKED_OUT = 3;

	/**
	 * ClearCase error indicating that a specific element is not checked out.
	 * (value is 4).
	 */
	public static final int ERROR_NOT_CHECKED_OUT = 4;

	/**
	 * ClearCase error indicating that a specific element is already added.
	 * (value is 5).
	 */
	public static final int ERROR_ALREADY_EXISTS = 5;

	/**
	 * ClearCase error constant indicating that a null argument was passed in
	 * (value is 6).
	 */
	public static final int ERROR_NULL_ARGUMENT = 6;

	/**
	 * ClearCase error constant indicating that an invalid argument was passed
	 * in (value is 7).
	 */
	public static final int ERROR_INVALID_ARGUMENT = 7;

	/**
	 * ClearCase error constant indicating that a particular feature has not
	 * been implemented on this platform (value is 8).
	 */
	public static final int ERROR_NOT_IMPLEMENTED = 8;

	/**
	 * ClearCase error constant indicating that an unsatisfied link error
	 * occured while attempting to load a library (value is 9).
	 */
	public static final int ERROR_FAILED_LOAD_LIBRARY = 9;

	/**
	 * ClearCase error constant indicating that an input/output operation failed
	 * during the execution of a ClearCase operation (value is 10).
	 */
	public static final int ERROR_IO = 10;

	/**
	 * ClearCase error constant indicating that an interface is disposed (value
	 * is 11).
	 */
	public static final int ERROR_INTERFACE_DISPOSED = 11;

	/**
	 * ClearCase error constant indicating that an operation was canceled (value
	 * is 12).
	 */
	public static final int ERROR_OPERATION_CANCELED = 12;

	/**
	 * ClearCase error indicating that a specific element is not accessible.
	 * (value is 13).
	 */
	public static final int ERROR_NOT_ACCESSIBLE = 13;

	/**
	 * ClearCase error indicating that a threads for reading i/o from clearcase
	 * was interrupted. (value is 14).
	 */
	public static final int ERROR_THREAD_INTERRUPTED = 14;

	/**
	 * ClearCase error indicating that you cannot modify directory since it is
	 * not checked-out. (value is 15).
	 */
	public static final int ERROR_DIR_IS_NOT_CHECKED_OUT = 15;

	/**
	 * ClearCase error indicating that predecessor is identical file being
	 * checked in.(value is 16).
	 */
	public static final int ERROR_PREDECESSOR_IS_IDENTICAL = 16;

	/**
	 * ClearCase error indicating that most recent version on branch is not the
	 * predecessor of this version.(value is 17).
	 */
	public static final int ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION = 17;

	/**
	 * ClearCase error indicating that parent directory is not checked
	 * out.(value is 18).
	 */
	public static final int ERROR_PARENT_IS_NOT_CHECKED_OUT = 18;

	/**
	 * ClearCase error indicating that the element has checkouts in a another
	 * view. (value is 19).
	 * 
	 */
	public static final int ERROR_ELEMENT_HAS_CHECKOUTS = 19;

	/**
	 * ClearCase error indicating that the element could not be created. (value
	 * is 20).
	 * 
	 */
	public static final int ERROR_CAN_NOT_CREATE_ELEMENT = 20;

	/**
	 * ClearCase error indicating that operation is not supported within a
	 * context e.g. snapshotview (value is 21).
	 * 
	 */
	public static final int ERROR_OPERATION_NOT_SUPPORTED = 21;

	/*
	 * NOTE: Good javadoc coding style is to put the values of static final
	 * constants in the comments. This reinforces the fact that consumers are
	 * allowed to rely on the value (and they must since the values are compiled
	 * inline in their code). We can <em> not </em> change the values of these
	 * constants between releases.
	 */

	/**
	 * Constant for using the ClearCase command line interface (value is 1).
	 * <p>
	 * <b>Used by: </b>
	 * <ul>
	 * <li><code>ClearCase</code></li>
	 * </ul>
	 * </p>
	 */
	public static final int INTERFACE_CLI = 1;

	/**
	 * Constant for using the ClearCase dialogs interface (value is 4).
	 * <p>
	 * <b>Used by: </b>
	 * <ul>
	 * <li><code>ClearCase</code></li>
	 * </ul>
	 * </p>
	 */
	public static final int INTERFACE_DIALOGS = 4;

	/**
	 * Constant for using the ClearCase native interface (value is 2).
	 * <p>
	 * <b>Used by: </b>
	 * <ul>
	 * <li><code>ClearCase</code></li>
	 * </ul>
	 * </p>
	 */
	public static final int INTERFACE_JNI = 2;

	/**
	 * Constant for using the ClearCase offline interface (value is 3).
	 * <p>
	 * <b>Used by: </b>
	 * <ul>
	 * <li><code>ClearCase</code></li>
	 * </ul>
	 * </p>
	 */
	public static final int INTERFACE_OFFLINE = 3;

	/**
	 * Constant for using the ClearCase command line interface with a single
	 * persistent cleartool process (value is 5).
	 * <p>
	 * <b>Used by: </b>
	 * <ul>
	 * <li><code>ClearCase</code></li>
	 * </ul>
	 * </p>
	 */
	public static final int INTERFACE_CLI_SP = 5;

	/**
	 * A constant known to be zero (0), used in operations which take bit flags
	 * to indicate that "no bits are set".
	 */
	public static final int NONE = 0;

	/**
	 * Operation constant for preserving times (value is 1&lt;&lt;1001). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int PTIME = 1 << 1001;

	/**
	 * Operation constant for working recursively (value is 1&lt;&lt;1002). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int RECURSIVE = 1 << 1002;

	/**
	 * Operation constant for forcing operations (value is 1&lt;&lt;1003). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int FORCE = 1 << 1003;

	/**
	 * Operation constant for perfoming an automatic merge on checkins if
	 * necessary (value is 1&lt;&lt;1004). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int MERGE = 1 << 1004;

	/**
	 * Operation constant for keeping a backup on undo checkouts (value is
	 * 1&lt;&lt;1005). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int KEEP = 1 << 1005;

	/**
	 * Operation constant for making an element the master of all replicas
	 * (value is 1&lt;&lt;1006). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int MASTER = 1 << 1006;

	/**
	 * Operation constant for performing a check in even if the version is
	 * identical to its predecessor (value is 1&lt;&lt;1007). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int IDENTICAL = 1 << 1007;

	/**
	 * Operation constant for performing a check in immediately after the
	 * operation (value is 1&lt;&lt;1008). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int CHECKIN = 1 << 1008;

	/**
	 * Operation content for listing only directory itself, not its content.<br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int DIR = 1 << 1009;

	/**
	 * Operation constant for uppressing checkout of the new directory element.
	 */
	public static final int NO_CHECK_OUT = 1 << 1010;

	/**
	 * Operation constant for performing the merge graphically.
	 */
	public static final int GRAPHICAL = 1 << 1011;

	/**
	 * ClearCase element state constant that indicates if an element is checked
	 * in (value is 1&lt;&lt;101).
	 */
	public static final int CHECKED_IN = 1 << 101;

	/**
	 * ClearCase element state constant that indicates if an element is checked
	 * out (value is 1&lt;&lt;102).
	 */
	public static final int CHECKED_OUT = 1 << 102;

	/**
	 * ClearCase element state constant that indicates if an element is view
	 * private (value is 1&lt;&lt;103).
	 */
	public static final int VIEW_PRIVATE = 1 << 103;

	/**
	 * ClearCase element state constant that indicates if an element is hijacked
	 * (value is 1&lt;&lt;104).
	 */
	public static final int HIJACKED = 1 << 104;

	/**
	 * ClearCase element state constant that indicates if an element is a link
	 * (value is 1&lt;&lt;105).
	 */
	public static final int LINK = 1 << 105;

	/**
	 * ClearCase element state constant that indicates if an element is missing
	 * (value is 1&lt;&lt;106).
	 */
	public static final int MISSING = 1 << 106;

	/**
	 * ClearCase element state and operation constant that indicates if a
	 * checked out is/must be reserved (value is 1&lt;&lt;107). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int RESERVED = 1 << 107;

	/**
	 * ClearCase element state and operation constant that indicates if a
	 * checked out is/must be unreserved (value is 1&lt;&lt;108). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int UNRESERVED = 1 << 108;

	/**
	 * ClearCase element state constant that indicates if an element is checked
	 * out in another branch (value is 1&lt;&lt;109).
	 */
	public static final int CHECKED_OUT_IN_DIFFERENT_BRANCH = 1 << 109;

	/**
	 * ClearCase element state constant that indicates if an element is checked
	 * out in another view (value is 1&lt;&lt;110).
	 */
	public static final int CHECKED_OUT_IN_DIFFERENT_VIEW = 1 << 110;

	/**
	 * ClearCase element state constant that indicates if an element is checked
	 * out by another user (value is 1&lt;&lt;111).
	 */
	public static final int CHECKED_OUT_BY_DIFFERENT_USER = 1 << 111;

	/**
	 * ClearCase element state constant that indicates if an element is not
	 * within a ClearCase vob (value is 1&lt;&lt;112).
	 */
	public static final int OUTSIDE_VOB = 1 << 112;

	/**
	 * View constant that indicates if a view is a snapshot view (value is
	 * 1&lt;&lt;113).
	 */
	public static final int SNAPSHOT = 1 << 113;

	/**
	 * View constant that indicates if a view is a snapshot view (value is
	 * 1&lt;&lt;114).
	 */
	public static final int DYNAMIC = 1 << 114;

	/**
	 * View constant that indicates if an file or directory is an clearcase
	 * element(value is 1&lt;&lt;115).
	 */
	public static final int IS_ELEMENT = 1 << 115;

	/**
	 * View constant that indicates if an file or directory is removed eg no
	 * longer an element. (value is 1&lt;&lt;115).
	 */
	public static final int REMOVED = 1 << 116;

	/**
	 * ClearCase element state constant that indicates if an element has been
	 * moved within a ClearCase vob (value is 1&lt;&lt;117).
	 */
	public static final int MOVED = 1 << 117;

	/**
	 * ClearCase element state constant that indicates if an element has been
	 * merged to latest version. (value is 1&lt;&lt;118).
	 */
	public static final int MERGED = 1 << 118;

	/**
	 * ClearCase element state and operation constant that indicates if a
	 * checked out should be reserved if possible (value is 1&lt;&lt;119). <br>
	 * Note that this is a <em>HINT</em>.
	 */
	public static final int RESERVED_IF_POSSIBLE = 1 << 119;

	/**
	 * ClearCase element state for derived objects (value is 1&lt;&lt;120). 
	 */
	public static final int DERIVED_OBJECT = 1 << 120;
	
	/**
	 * Throws an appropriate exception based on the passed in error code.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @see #error(int, Throwable, String, String[], ClearCaseElementState[])
	 */
	public static void error(final int code) {
		error(code, null, null, null, null);
	}

	/**
	 * Throws an appropriate exception based on the passed in error code.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param detail
	 *            a detail message
	 * @see #error(int, Throwable, String, String[], ClearCaseElementState[])
	 */
	public static void error(final int code, final String detail) {
		error(code, null, detail, null, null);
	}

	/**
	 * Throws an appropriate exception based on the passed in error code. The
	 * <code>elements</code> argument is only relevant for the non-fatal
	 * <code>ClearCaseException</code> and it is optional.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param elements
	 *            the affected elements (maybe <code>null</code>)
	 * @param elementStates
	 *            the element states of already modified elements
	 * @see #error(int, Throwable, String, String[], ClearCaseElementState[])
	 */
	public static void error(final int code, final String[] elements,
			final ClearCaseElementState[] elementStates) {
		error(code, null, null, elements, elementStates);
	}

	/**
	 * Throws an appropriate exception based on the passed in error code. The
	 * <code>throwable</code> argument should be either <code>null</code>, or
	 * the throwable which caused ClearCase to throw an exception.
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param throwable
	 *            the exception which caused the error to occur
	 * @param elementStates
	 *            the element states of already modified elements
	 * @see #error(int, Throwable, String, String[], ClearCaseElementState[])
	 */
	public static void error(final int code, final Throwable throwable,
			final ClearCaseElementState[] elementStates) {
		error(code, throwable, null, null, elementStates);
	}

	/**
	 * Throws an appropriate exception based on the passed in error code. The
	 * <code>throwable</code> argument should be either <code>null</code>, or
	 * the throwable which caused ClearCase to throw an exception.
	 * <p>
	 * The <code>elements</code> argument is only relevant for the non-fatal
	 * <code>ClearCaseException</code> and it is optional.
	 * </p>
	 * <p>
	 * In ClearCase, errors are reported by throwing one of three exceptions:
	 * <dl>
	 * <dd>java.lang.IllegalArgumentException</dd>
	 * <dt>thrown whenever one of the API methods is invoked with an illegal
	 * argument</dt>
	 * <dd>net.sourceforge.clearcase.ClearCaseException (extends
	 * java.lang.RuntimeException)</dd>
	 * <dt>thrown whenever a recoverable error happens internally in ClearCase</dt>
	 * <dd>net.sourceforge.clearcase.ClearCaseError (extends java.lang.Error)</dd>
	 * <dt>thrown whenever a <b>non-recoverable </b> error happens internally in
	 * ClearCase</dt>
	 * </dl>
	 * This method provides the logic which maps between error codes and one of
	 * the above exceptions.
	 * </p>
	 * 
	 * @param code
	 *            the ClearCase error code
	 * @param throwable
	 *            the exception which caused the error to occur (maybe
	 *            <code>null</code>)
	 * @param detail
	 *            more information about error
	 * @param elements
	 *            the affected elements (maybe <code>null</code>)
	 * @param elementStates
	 *            the element states of already modified elements
	 * 
	 * @see ClearCaseError
	 * @see ClearCaseException
	 * @see IllegalArgumentException
	 */
	public static void error(final int code, final Throwable throwable,
			final String detail, final String[] elements,
			final ClearCaseElementState[] elementStates) {

		/*
		 * This code prevents the creation of "chains" of ClearCaseErrors and
		 * ClearCaseExceptions which in turn contain other ClearCaseErrors and
		 * ClearCaseExceptions as their throwable. This can occur when low level
		 * code throws an exception past a point where a higher layer is being
		 * "safe" and catching all exceptions. (Note that, this is _a_bad_thing_
		 * which we always try to avoid.)
		 * 
		 * On the theory that the low level code is closest to the original
		 * problem, we simply re-throw the original exception here.
		 */
		if (throwable instanceof ClearCaseError)
			throw (ClearCaseError) throwable;
		else if (throwable instanceof ClearCaseException)
			throw (ClearCaseException) throwable;

		String message = findErrorText(code);
		if (detail != null) {
			message += ": " + detail; //$NON-NLS-1$
		}
		switch (code) {

		/* Illegal Arguments (non-fatal) */
		case ERROR_NULL_ARGUMENT:
		case ERROR_INVALID_ARGUMENT:
			throw new IllegalArgumentException(message);

			/* ClearCase Errors (non-fatal) */
		case ERROR_ALREADY_EXISTS:
		case ERROR_ALREADY_CHECKED_OUT:
		case ERROR_NOT_AN_ELEMENT:
		case ERROR_NOT_CHECKED_OUT:
		case ERROR_IO:
		case ERROR_INTERFACE_DISPOSED:
		case ERROR_NOT_ACCESSIBLE:
		case ERROR_OPERATION_CANCELED:
		case ERROR_PREDECESSOR_IS_IDENTICAL:
		case ERROR_ELEMENT_HAS_CHECKOUTS:
		case ERROR_CAN_NOT_CREATE_ELEMENT:
		case ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION:
		case ERROR_OPERATION_NOT_SUPPORTED:
			throw new ClearCaseException(code, message, throwable, elements,
					elementStates);
			/* OS Failure/Limit (fatal, may occur only on some platforms) */
		case ERROR_EXCEPTION:
			// FALL THROUGH

			/*
			 * ClearCase Failure/Limit (fatal, may occur only on some platforms)
			 */
		case ERROR_FAILED_LOAD_LIBRARY:
		case ERROR_NOT_IMPLEMENTED:
		case ERROR_UNSPECIFIED:
		default:
			ClearCaseError error = new ClearCaseError(code, message, throwable);
			throw error;

		}
	}

	/**
	 * This enables logging to the console.
	 */
	static void configureLoggerForDebugging() {

		/**
		 * A handler that logs detail messages (infos and higher) to
		 * <code>System.out</code>.
		 */
		class SystemOutConsoleHandler extends ConsoleHandler {
			/**
			 * 
			 * Creates a new instance.
			 */
			SystemOutConsoleHandler() {
				super();
				setLevel(Level.ALL);
				setFilter(new Filter() {

					public boolean isLoggable(final LogRecord record) {
						// only log infos and fine messages
						return record.getLevel().intValue() <= Level.INFO
								.intValue();
					}
				});
			}

			@Override
			protected synchronized void setOutputStream(OutputStream out)
					throws SecurityException {
				super.setOutputStream(System.out);
			}
		}

		// configure logging during development
		Logger logger = Logger
				.getLogger(ClearCase.class.getPackage().getName());
		logger.setLevel(Level.ALL);

		// warnings and higher to System.err
		ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.WARNING);
		logger.addHandler(handler);

		// infors and lower to System.out
		logger.addHandler(new SystemOutConsoleHandler());
	}

	/**
	 * Answers a concise, human readable description of the error code.
	 * 
	 * @param code
	 *            the ClearCase error code.
	 * @return a description of the error code.
	 * 
	 * @see ClearCase
	 */
	static String findErrorText(final int code) {
		switch (code) {
		case ERROR_UNSPECIFIED:
			return "Unspecified error"; //$NON-NLS-1$
		case ERROR_NULL_ARGUMENT:
			return "Argument cannot be null"; //$NON-NLS-1$
		case ERROR_NOT_CHECKED_OUT:
			return "Unspecified error"; //$NON-NLS-1$
		case ERROR_NOT_AN_ELEMENT:
			return "File or directory is not an element"; //$NON-NLS-1$
		case ERROR_ALREADY_EXISTS:
			return "Element already exists"; //$NON-NLS-1$
		case ERROR_ALREADY_CHECKED_OUT:
			return "Element is already checked out."; //$NON-NLS-1$
		case ERROR_FAILED_LOAD_LIBRARY:
			return "Unable to load library"; //$NON-NLS-1$
		case ERROR_NOT_IMPLEMENTED:
			return "Not implemented"; //$NON-NLS-1$
		case ERROR_INVALID_ARGUMENT:
			return "Argument not valid"; //$NON-NLS-1$
		case ERROR_IO:
			return "i/o error"; //$NON-NLS-1$
		case ERROR_INTERFACE_DISPOSED:
			return "Interface disposed"; //$NON-NLS-1$
		case ERROR_OPERATION_CANCELED:
			return "Operation canceled"; //$NON-NLS-1$
		case ERROR_NOT_ACCESSIBLE:
			return "Unable to access"; //$NON-NLS-1$
		case ERROR_ELEMENT_HAS_CHECKOUTS:
			return "Element has checkouts in another view!";
		case ERROR_PREDECESSOR_IS_IDENTICAL:
			return "Predecessor is identical";
		case ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION:
			return "The most recent version on branch is not the predecessor of this version";
		case ERROR_CAN_NOT_CREATE_ELEMENT:
			return "Unable to add element.";
		default:
			return "Unknown error"; //$NON-NLS-1$
		}
	}

	/**
	 * The hidden constructor.
	 */
	private ClearCase() {
		// empty
	}

	/**
	 * Creates and returns a ClearCase interface of the specified type.
	 * <p>
	 * <code>null</code> is returned if the interface is not supported on the
	 * current platform. <br>
	 * The following interfaces a currently supported:
	 * <ul>
	 * <li><code>INTERFACE_CLI</code>-<em>command line interface</em> <br>
	 * The command line interface is available on all platforms. It supports
	 * ClearCase 4, 5 and 6.</li>
	 * <li><code>INTERFACE_JNI</code>-<em>native interface</em><br>
	 * The native interface is available on Windows only. It supports ClearCase
	 * 4, 5 and 6.</li>
	 * <li><code>INTERFACE_OFFLINE</code>-<em>offline interface</em><br>
	 * The offline interface is available on all platforms.</li>
	 * <li><code>INTERFACE_DIALOGS</code>-<em>dialogs interface</em><br>
	 * The dialogs interface is available on Windows only. It supports ClearCase
	 * 4, 5 and 6.</li>
	 * </ul>
	 * <br>
	 * <strong>HELP WANTED: </strong> Someone may contribute a INTERFACE_JNI
	 * based native interface for other platforms.
	 * </p>
	 * 
	 * @param type
	 *            the interface type to create
	 * @see #INTERFACE_CLI
	 * @see #INTERFACE_CLI_SP
	 * @see #INTERFACE_JNI
	 * @see #INTERFACE_OFFLINE
	 * @see #INTERFACE_DIALOGS
	 * @return the ClearCase interface (maybe <code>null</code>)
	 */
	public static ClearCaseInterface createInterface(final int type) {

		/*
		 * To have a clean coding style all OS-specific interfaces are created
		 * using reflection. Thus, no dependencies to OS-specific
		 * implementations exists.
		 */

		switch (type) {
		case INTERFACE_CLI:
			// configureLoggerForDebugging();
			return new ClearCaseCLIImpl();

		case INTERFACE_CLI_SP:
			// configureLoggerForDebugging();
			ClearCaseInterface iface = new ClearCaseCLIImpl();
			iface.setSingleprocessLauncher(true);
			return iface;

		case INTERFACE_JNI:
			// if (Os.isFamily(Os.WINDOWS)) {
			// return new ClearCaseWindowsJNIImpl(this);
			// }
			// break;

		case INTERFACE_OFFLINE:
			// return new ClearCaseOfflineImpl(this);
			// break;

		case INTERFACE_DIALOGS:
			// if (Os.isFamily(Os.WINDOWS)) {
			// return new ClearCaseWindowsDialogsImpl(this);
			// }
			// break;
		default:
			// no implementation available for the current platform
			return null;
		}
	}

}
