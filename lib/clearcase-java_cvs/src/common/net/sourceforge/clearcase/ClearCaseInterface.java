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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import net.sourceforge.clearcase.events.DisposeListener;
import net.sourceforge.clearcase.events.OperationListener;
import net.sourceforge.clearcase.utils.ListenerList;

/**
 * This class defines the API for operating with ClearCase.
 * <p>
 * Multiple implementations are available and not every implementation supports
 * every operation exposed in this interface. Use
 * {@link net.sourceforge.clearcase.ClearCase#createInterface(int)}to create
 * interface implementations of a specific type.
 * </p>
 * <p>
 * ClearCase interfaces <b>must be disposed </b> when they are no longer needed
 * to clean up system resources used during operations.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public abstract class ClearCaseInterface {

	/**
	 * Data key constant for accessing the ClearCase version value. The value is
	 * a <code>String</code>.
	 */
	public static final Object CLEAR_CASE_VERSION = new Object();

	/**
	 * Data key constant for accessing the ClearCase home directory value. The
	 * value is a <code>String</code>.
	 */
	public static final Object CLEAR_CASE_HOME = new Object();

	/**
	 * Data key constant for accessing the default working directory value. The
	 * value is a <code>String</code>.
	 */
	public static final Object WORKING_DIR = new Object();

	/**
	 * flag indicating the disposed state of this interface
	 */
	private static final int DISPOSED = 1 << 0;

	/** the logger */
	public static final Logger LOG = Logger.getLogger(ClearCaseInterface.class
			.getName());

	/**
	 * Dynamic view type constant, result from the
	 * {@link ClearCaseInterface#getViewType()} command.
	 */
	public static final String VIEW_TYPE_DYNAMIC = "dynamic";

	/**
	 * Snapshot view type constant, result from the
	 * {@link ClearCaseInterface#getViewType()} command.
	 */
	public static final String VIEW_TYPE_SNAPSHOT = "snapshot";

	/**
	 * minimum shift for client flags <br>
	 * usage: <code>A_CLIENT_FLAG = 1 &lt;&lt; (CLIENT_FLAGS+n)</code>
	 */
	protected static final int CLIENT_FLAGS = 1024;

	/**
	 * Indicates if a given flag is set in the specified flags value.
	 * 
	 * @param flags
	 *            the complete flags
	 * @param flag
	 *            the flag constant to check
	 * @return <code>true</code> if the given flag is set, <code>false</code>
	 *         otherwise
	 */
	protected static final boolean isSet(int flags, int flag) {
		return (flags & flag) == flag;
	}

	/** the interface state */
	private int flags;

	/** the list of dispose listeners */
	private ListenerList disposeListerners;

	/** interface specific data */
	private Map data;

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the interface is disposed. When the interface is disposed, the
	 * listener is notified by sending it the <code>interfaceDisposed()</code>
	 * message.
	 * 
	 * @param listener
	 *            the listener which should be notified when the receiver is
	 *            disposed
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * 
	 * @see DisposeListener
	 * @see #removeDisposeListener
	 */
	public void addDisposeListener(DisposeListener listener) {

		// check arguments
		checkInterface();
		if (listener == null) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT);
		}

		// lazy initialize
		if (null == disposeListerners) {
			synchronized (this) {
				if (null == disposeListerners) {
					disposeListerners = new ListenerList();
				}
			}
		}

		// add listener
		disposeListerners.add(listener);
	}

	/**
	 * Removes the listener from the collection of listeners who will be notifed
	 * when the interface is disposed.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified when the
	 *            receiver is disposed
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @see DisposeListener
	 * @see #addDisposeListener
	 */
	public void removeDisposeListener(DisposeListener listener) {

		// check arguments
		if (listener == null) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT);
		}

		// If disposed or no listeners registered, fail silently.
		if (getState(DISPOSED) || null == disposeListerners)
			return;

		// remove listener
		disposeListerners.remove(listener);
	}

	/**
	 * Returns <code>true</code> if the interface has been disposed, and
	 * <code>false</code> otherwise.
	 * <p>
	 * This method gets the dispose state for the interface. When an interface
	 * has been disposed, it is an error to invoke any other method using the
	 * interface.
	 * </p>
	 * 
	 * @return <code>true</code> when the interface is disposed and
	 *         <code>false</code> otherwise
	 */
	public boolean isDisposed() {
		return getState(DISPOSED);
	}

	/**
	 * Disposes of the operating system resources associated with the receiver
	 * and all its descendants. After this method has been invoked, the receiver
	 * and all descendants will answer <code>true</code> when sent the message
	 * <code>isDisposed()</code>. Any internal connections between the interface
	 * will have been removed to facilitate garbage collection.
	 * 
	 * @see #addDisposeListener
	 * @see #removeDisposeListener
	 */
	public final void dispose() {
		/*
		 * Note: It is valid to attempt to dispose an interface more than once.
		 * If this happens, fail silently.
		 */
		if (isDisposed())
			return;

		try {
			// perform disposing
			LOG.fine("diposing interface '" + getName() + "'"); //$NON-NLS-1$//$NON-NLS-2$
			doDispose();
		} finally {
			// mark as disposed
			setState(DISPOSED, false);
		}

		// notify listeners
		if (null != disposeListerners) {
			Object[] listeners = disposeListerners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				try {
					((DisposeListener) listeners[i]).interfaceDisposed(this);
				} catch (Throwable t) {
					LOG.severe("Error in dispose listerner: '" //$NON-NLS-1$
							+ t.getMessage() + "' (" + t.getClass().getName() //$NON-NLS-1$
							+ ")"); //$NON-NLS-1$
					LOG.throwing("ClearCaseInterface", "interfaceDisposed", t); //$NON-NLS-1$//$NON-NLS-2$
					removeDisposeListener((DisposeListener) listeners[i]);
				}
			}
		}
	}

	/**
	 * Performs disposing of the operating system resources associated with the
	 * receiver and all its descendants. Clients should remove any internal
	 * connections between the interface and other low-level ClearCase element
	 * to facilitate garbage collection.
	 * <p>
	 * Implementors must also call the super implementation.
	 * </p>
	 */
	protected void doDispose() {
		// nothing to do here (open for future extensions)
	}

	/**
	 * Throws an <code>ClearCaseException</code> if the receiver can not be
	 * accessed by the caller. This may include both checks on the state of the
	 * receiver and more generally on the entire execution context. This method
	 * <em>should</em> be called by interface implementors to enforce the
	 * standard ClearCase invariants.
	 * <p>
	 * Currently, it is an error to invoke any method (other than
	 * <code>isDisposed</code> or <code>removeDisposeListener</code>) on an
	 * interface that has had its <code>dispose()</code> method called.
	 * </p>
	 * <p>
	 * In future releases of the ClearCase API, there may be more or fewer error
	 * checks and exceptions may be thrown for different reasons.
	 * </p>
	 * 
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 */
	protected void checkInterface() {
		if (getState(DISPOSED)) {
			ClearCase.error(ClearCase.ERROR_INTERFACE_DISPOSED);
		}
	}

	/**
	 * Returns the state of the specified flag.
	 * 
	 * @param state
	 * @return <code>true</code> if the flag is set (enabled),
	 *         <code>false</code> otherwise
	 */
	protected boolean getState(int state) {
		return (this.flags & state) == state;
	}

	/**
	 * Sets the state of the specified flag.
	 * 
	 * @param flag
	 *            the flag
	 * @param enable
	 *            <code>true</code> to set (enable) the flag, <code>false</code>
	 *            to unset (disable) the flag
	 */
	protected void setState(int flag, boolean enable) {
		if (enable) {
			this.flags |= flag;
		} else {
			this.flags &= ~flag;
		}
	}

	/**
	 * Returns the application defined property of the receiver with the
	 * specified name, or null if it has not been set.
	 * <p>
	 * Applications may have associated arbitrary objects with the receiver in
	 * this fashion. If the objects stored in the properties need to be notified
	 * when the interface is disposed of, it is the application's responsibility
	 * to hook the Dispose event on the interface and do so.
	 * </p>
	 * 
	 * @param key
	 *            the key of the property
	 * @return the value of the property or null if it has not been set
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #setData
	 */
	public Object getData(Object key) {
		checkInterface();
		if (key == null) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT);
		}
		if (null != data)
			return data.get(key);
		return null;
	}

	/**
	 * Sets the application defined property of the receiver with the specified
	 * name to the given value.
	 * <p>
	 * Applications may associate arbitrary objects with the receiver in this
	 * fashion. If the objects stored in the properties need to be notified when
	 * the interface is disposed of, it is the application's responsibility to
	 * hook the Dispose event on the interface and do so.
	 * </p>
	 * 
	 * @param key
	 *            the key of the property
	 * @param value
	 *            the new value for the property
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the key is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 * @see #getData
	 */
	@SuppressWarnings("unchecked")
	public void setData(Object key, Object value) {
		checkInterface();
		if (key == null) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT);
		}
		// lazy initialize map
		synchronized (this) {
			if (null == data) {
				data = Collections.synchronizedMap(new HashMap(4));
			}
		}

		if (null != value) {
			// store
			data.put(key, value);
		} else {
			// value is null, remove the key
			data.remove(key);
		}
	}

	/**
	 * Returns a human readable name of this interface.
	 * 
	 * @return a human readable name of this interface
	 */
	public String getName() {
		return Messages.getString("ClearCaseInterface.name.unknown"); //$NON-NLS-1$
	}

	/**
	 * Returns the element state of a single element.
	 * <p>
	 * If the element is a directory only the directory's state is returned.
	 * This is equivalent to <code>cleartool ls -dir</code>.
	 * </p>
	 * 
	 * @param element
	 *            the element
	 * @return the element state of the specified element
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the element is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 */
	public abstract ClearCaseElementState getElementState(String element);

	/**
	 * Returns the element states of elements contained in a directory.
	 * <p>
	 * If the <code>RECURSIVE</code> flag is set, also the states of any
	 * indirect children will be collected (equivalent to
	 * <code>cleartool ls -r</code>.
	 * </p>
	 * <p>
	 * If the <code>RECURSIVE</code> flag is not set and one of the elements is
	 * a directory then the states of this direcotories children will be
	 * returned. If you are interested in the state of the directory element
	 * itself see {@link #getElementState(String)}.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of the specified elements
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#RECURSIVE
	 */
	public abstract ClearCaseElementState[] getElementStates(String[] elements,
			int flags, OperationListener operationListener);

	/**
	 * Does a checkout of the specified files.
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>RESERVED</code> flag and the <code>FORCE</code> flag is set, the
	 * elements will be checked out unreserved if a reserved checkout fails.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param comment
	 *            a comment (maybe <code>null</code>)
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_ALREADY_CHECKED_OUT - if an element is already
	 *                checked out</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if an element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#RESERVED
	 * @see ClearCase#UNRESERVED
	 * @see ClearCase#FORCE
	 * @see ClearCase#PTIME
	 */
	public abstract ClearCaseElementState[] checkout(String[] elements,
			String comment, int flags, OperationListener operationListener);

	/**
	 * Does a checkin of the specified files.
	 * <p>
	 * Comment can be empty string or <code>null</code>. The <code>FORCE</code>
	 * flag is interpreted as <code>IDENTICAL</code>.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param comment
	 *            a comment (maybe <code>null</code>)
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_NOT_CHECKED_OUT - if an element was not checked
	 *                out</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if an element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                <li>ERROR_PREDECESSOR_IS_IDENTICAL - when data is
	 *                identical to predecessor</li>
	 *                </ul>
	 * 
	 * @see ClearCase#PTIME
	 * @see ClearCase#FORCE
	 * @see ClearCase#IDENTICAL
	 */
	public abstract ClearCaseElementState[] checkin(String[] elements,
			String comment, int flags, OperationListener operationListener)
			throws ClearCaseException;

	/**
	 * Does a uncheckout of the specified files.
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>KEEP</code> flag is set, backups of the checked out version ending
	 * with <code>.keep</code> will be created.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_NOT_CHECKED_OUT - if an element was not checked
	 *                out</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if an element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#RECURSIVE
	 * @see ClearCase#KEEP
	 */
	public abstract ClearCaseElementState[] uncheckout(String[] elements,
			int flags, OperationListener operationListener);

	/**
	 * Adds the specified element to ClearCase source control.
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>FORCE</code> flag is set the parent element will be added if
	 * necessary and checked out, otherwise it must be checked out before
	 * calling this method. The <code>CHECKIN</code> may be used to check in
	 * files after adding them. Only one element at time since current design
	 * does not support many.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param comment
	 *            a comment (maybe <code>null</code>)
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_ALREADY_ADDED - if an element was already added</li>
	 *                <li>ERROR_NOT_CHECKED_OUT - if a parent element was not
	 *                checked out</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if a parent element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#PTIME
	 * @see ClearCase#FORCE
	 * @see ClearCase#CHECKIN
	 * @see ClearCase#MASTER
	 */
	public abstract ClearCaseElementState add(String element,
			boolean isDirectory, String comment, int flags,
			OperationListener operationListener);

	/**
	 * 
	 * @param element
	 * @param group
	 * @param operationListener
	 * @return
	 */
	public abstract ClearCaseElementState setGroup(String element,
			String group, OperationListener operationListener);

	/**
	 * Removes the specified elements from a directory version. This does not
	 * remove the complete element. It will be only removed from the parent
	 * directories version.
	 * <p>
	 * Comment can be empty string or <code>null</code>. The operation will fail
	 * if there are checkouts of the file and the <code>FORCE</code> flag is not
	 * set.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param comment
	 *            a comment (maybe <code>null</code>)
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_ALREADY_CHECKED_OUT - if an element has check
	 *                outs</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if an element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_CHECKED_OUT - if a parent element was not
	 *                checked out</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#FORCE
	 */
	public abstract ClearCaseElementState[] delete(String[] elements,
			String comment, int flags, OperationListener operationListener);

	/**
	 * Moves or renames the specified element to the given target element. Each
	 * element must have a corresponding target entry (the sizes of the element
	 * and target arrays must be identical). The operation fails if a target for
	 * an element is not within the same VOB of that element.
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>FORCE</code> flag is set the parent element will be checked out
	 * otherwise it must be checked out before calling this method.
	 * <code>CHECKIN</code> may be used to check in all checked out parent
	 * directories after removing.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param element
	 * 
	 * @param target
	 * 
	 * @param comment
	 *            a comment (maybe <code>null</code>)
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of all elements that changed their state
	 *         during the operation
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                <li>ERROR_NULL_ARGUMENT - if the targets is null</li>
	 *                <li>ERROR_INVALID_ARGUMENT - if the size of the source and
	 *                targets is not identical</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_NOT_CHECKED_OUT - if a parent element was not
	 *                checked out</li>
	 *                <li>ERROR_NOT_AN_ELEMENT - if an element is not a
	 *                ClearCase element</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#PTIME
	 * @see ClearCase#FORCE
	 * @see ClearCase#CHECKIN
	 */
	public abstract ClearCaseElementState[] move(String element, String target,
			String comment, int flags, OperationListener operationListener);

	/**
	 * Updates the specified elements in a snapshot view.
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>KEEP</code> flag is set all hijacked files well be renamed to
	 * <code>filename.keep</code>. If the <code>FORCE</code> flag is set but the
	 * <code>KEEP</code> is not set, all hijacked files will be overwritten with
	 * the version selected by the config spec. If the <code>FORCE</code> flag
	 * is not set hijacked files will be left in the view with their current
	 * modifiactions.
	 * </p>
	 * 
	 * @param element
	 * 
	 * @param flags
	 *            the flags for performing the operation
	 * @param isWorkingDir
	 *            if true then element is the workingDir else a single element.
	 * 
	 * @see ClearCase#FORCE
	 * @see ClearCase#PTIME
	 * @see ClearCase#KEEP
	 */
	public abstract void update(String element, int flags, boolean isWorkingDir);

	/**
	 * Indicates if the element is checked out in the current view and differs
	 * from its predecessor.
	 * 
	 * @param element
	 * @return <code>true</code> if the element is checked out and differs from
	 *         its predecessor
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract boolean isDifferent(String element);

	/**
	 * Finds all checkouts of the specified elements.
	 * <p>
	 * If the <code>RECURSIVE</code> flag is set, also the states of any
	 * indirect children will be collected (equivalent to
	 * <code>cleartool lsco -r</code>. If the <code>RECURSIVE</code> flag is not
	 * set the output is equivalent to <code>cleartool lsco -d</code>.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param elements
	 *            list of elements
	 * @param flags
	 *            the flags for performing the operation
	 * @param operationListener
	 *            an operation listener (maybe <code>null</code>)
	 * @return list of element states of the specified elements
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the elements is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_OPERATION_CANCELED - if the operation was
	 *                canceled</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#RECURSIVE
	 */
	public abstract ClearCaseElementState[] findCheckouts(String[] elements,
			int flags, OperationListener operationListener);

	// public abstract ClearCaseElementState ccElement(String element, int
	// flags,
	// OperationListener operationListener);

	/**
	 * Returns the name of the view the element is contained in.
	 * 
	 * @param element
	 *            the element
	 * @return the view name, empty string if element is not inside a view
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the element is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract String getViewName(String element);

	/**
	 * Returns the file system location of the view with the specified name.
	 * 
	 * @param viewName
	 *            the view name
	 * @return the view root
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the view name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract String getViewLocation(String viewName);

	/**
	 * Returns the type of view, snapshot or dynamic, for the view with the name
	 * given as parameter.
	 * 
	 * @param viewName
	 *            The name of the view for which to get the type.
	 * @return {@link ClearCaseInterface#VIEW_TYPE_DYNAMIC} or
	 *         {@link ClearCaseInterface#VIEW_TYPE_SNAPSHOT}.
	 */
	public abstract String getViewType(String viewName);

	//

	/**
	 * Fix for Bug 2509230. Returns the type of view, snapshot or dynamic, for
	 * the view with the name given as parameter.
	 * 
	 * @param viewName
	 *            The name of the view for which to get the type.
	 * @param element
	 * @return {@link ClearCase#DYNAMIC} or {@link ClearCase#SNAPSHOT}.
	 */
	public abstract ClearCaseElementState getViewType(String viewName,
			String element);

	/**
	 * Returns the flags of the specified view.
	 * 
	 * @param viewName
	 *            the view name
	 * @return the view flags
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the view name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * @see ClearCase#SNAPSHOT
	 * @see ClearCase#DYNAMIC
	 */
	public abstract int getViewFlags(String viewName);

	/**
	 * Returns the config spec of the specified view.
	 * 
	 * @param viewName
	 *            the view name
	 * @return the config spec
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the view name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract String getViewConfigSpec(String viewName);

	/**
	 * Returns the history string of the elemtent.
	 * 
	 * @param element
	 *            the element
	 * @return History Vector<ElementHistory>
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the element is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract Vector<ElementHistory> getElementHistory(String element);

	/**
	 * Sets the content of the specified file as the config spec of the
	 * specified view.
	 * <p>
	 * If the file name is <code>null</code>, the config spec will be resetted
	 * to the view's default.
	 * </p>
	 * <p>
	 * <i>In a snapshot view, <code>setcs</code> initiates an
	 * <code>update -noverwrite</code> operation for the current view and
	 * generates an update logfile with the default name and location. </i>
	 * </p>
	 * 
	 * @param viewName
	 *            the view name
	 * @param configSpecFile
	 *            the config spec file (maybe <code>null</code>)
	 * @param listener
	 *            an operation lister to control the setViewConfigSpec
	 *            execution.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the view name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract void setViewConfigSpec(String viewName,
			String configSpecFile, OperationListener listener);
	
	/**
	 * Sets the content of the specified file as the config spec of the
	 * specified view.
	 * <p>
	 * If the file name is <code>null</code>, the config spec will be resetted
	 * to the view's default.
	 * </p>
	 * <p>
	 * <i>In a snapshot view, <code>setcs</code> initiates an
	 * <code>update -noverwrite</code> operation for the current view and
	 * generates an update logfile with the default name and location. </i>
	 * </p>
	 * 
	 * @param viewName
	 *            the view name
	 * @param configSpecFile
	 *            the config spec file (maybe <code>null</code>)
	 * @param workingDriectory
	 *            the current working directory
	 * @param listener
	 *            an operation lister to control the setViewConfigSpec
	 *            execution.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the view name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract void setViewConfigSpec(String viewName,
			String configSpecFile, String workingDriectory, OperationListener listener);

	/**
	 * Returns the list of all views available in the view registry.
	 * 
	 * @return the view names
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 */
	public abstract String[] getViewNames();

	/**
	 * Returns the list of all vobs available in the current network area.
	 * 
	 * @return the vob names
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                </ul>
	 * 
	 */
	public abstract String[] getVobNames(OperationListener operationListener);

	/**
	 * List view private files on a dynamic view.
	 * 
	 * @param workingdir
	 *            The directory to start in.
	 * @param operationListener
	 *            Operation listener.
	 * @return An array of {@link ClearCaseElementState} for the view private
	 *         and checked out files.
	 */
	public abstract ClearCaseElementState[] getViewLSPrivateList(
			String workingdir, OperationListener operationListener);

	/**
	 * List view only files, works both on snapshot and dynamic views.
	 * 
	 * @param workingdir
	 *            The directory from which to find view only files.
	 * @param operationListener
	 *            Operation listener.
	 * @return An array of {@link ClearCaseElementState} for the view private
	 *         and checked out files.
	 */
	public abstract ClearCaseElementState[] getViewLSViewOnlyList(
			String workingdir, OperationListener operationListener);

	/**
	 * List checked out files on a view (lsco -cview).
	 * 
	 * @param workingdir
	 *            The directory to start in
	 * @param operationlistener
	 *            Operation listener, optional.
	 * @return An array of {@link ClearCaseElementState} for the checked out
	 *         files and directories.
	 */
	public abstract ClearCaseElementState[] getCheckedOutElements(
			String workingdir, OperationListener operationlistener);

	/**
	 * Mounts the specified vob.
	 * 
	 * @param vobName
	 *            the vob name
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract void mountVob(String vobName);

	/**
	 * Un-mounts the specified vob.
	 * 
	 * @param vobName
	 *            the vob name
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public abstract void unmountVob(String vobName);

	/**
	 * The lsvtree command lists all of the version tree of one element.
	 * 
	 * @param element
	 *            to list versions for.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * 
	 * @return a list of versions.
	 */
	public abstract String[] listAllVersionsOfElement(String element);

	/**
	 * The merge command calls an element-type-specific program (the merge
	 * method) to merge the contents of two or more files, or two or more
	 * directories. Typically the files are versions of the same file element.
	 * 
	 * @param targetPath
	 * @param contributor
	 *            versions.
	 * @param flags
	 *            .
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * 
	 * @return a list of versions.
	 */
	public abstract void merge(String targetPath, String[] contributorVersions,
			int flags);

	/**
	 * To get previous version we use the 'desc -s -pre' command. Out put is
	 * version \main\41 (win32) or /main/41 (Unix).
	 * 
	 * @param element
	 *            path
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 * 
	 * @return a list of versions.
	 */
	public abstract String getPreviousVersion(String element);

	/**
	 * Shows a clearcase 'lsvtree' graphical for an element.
	 * 
	 * @param element
	 */
	public abstract void showVersionTree(String element);
	

	/**
	 * Shows a clearcase 'lsvtree' graphical for an element.
	 * 
	 * @param element
	 * @param workingDir
	 */
	public abstract void showVersionTree(String element, File workingDir);

	/**
	 * Shows a clearcase 'findmerge' graphical for an element.
	 * 
	 * @param workingDir
	 *            directory to start in
	 */
	public abstract void showFindMerge(File workingDir);

	/**
	 * Shows a clearcase 'diff' graphical between selected vesion and its
	 * predecessor.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * 
	 * @param element
	 */
	public abstract void compareWithPredecessor(String element);

	/**
	 * Shows a clearcase 'describe' graphical
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * 
	 * @param element
	 */
	public abstract void describeVersionGUI(String element);

	/**
	 * Shows a clearcase 'diff' graphical between two versions of an element.
	 * 
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the vob name is null</li>
	 *                </ul>
	 * @param element1
	 * @param element2
	 */
	public abstract void compareWithVersion(String element1, String element2);

	/**
	 * Search Files that has been modified into a branch.
	 * 
	 * @param branchName Name of the branch
	 * @param workingDir Clearcase working directory
	 * @return Files into the branch
	 */
	public abstract String[] searchFilesInBranch(String branchName, File workingDir, OperationListener listener);
	
	
	public abstract String[] loadBrancheList(File workingDir);
	
	public abstract void setSingleprocessLauncher(
			boolean useSingleprocessLauncher);

	public abstract void setDebugLevel(int level);
	
	/**
	 * For a checked-out element it will return "reserved",
     * "unreserved", or nothing "" (if it is not checked out) 
	 * @param elemment
	 * @return state
	 */
	public abstract String checkedOutType(String elemment);
	
	/**
	 * For a checked-out element it will change the state to
	 * reserved.
	 * @param elements
	 * @param comment
	 * @param flags
	 * @param operationListener
	 * @return
	 */
	public abstract ClearCaseElementState[] reserved(String[] elements,
			String comment, int flags,
			OperationListener operationListener);
	
	/**
	 * For a checked-out element it will change the state to
	 * unreserved.
	 * @param elements
	 * @param comment
	 * @param flags
	 * @param operationListener
	 * @return
	 */
	public abstract ClearCaseElementState[] unreserved(String[] elements,
			String comment, int flags,
			OperationListener operationListener);
	

}
