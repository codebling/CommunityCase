package net.sourceforge.clearcase;



public interface Disposable {
	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the interface is disposed. When the interface is disposed, the listener
	 * is notified by sending it the <code>interfaceDisposed()</code> message.
	 * 
	 * @param listener
	 *            the listener which should be notified when the receiver is
	 *            disposed
	 * 
	 * @exception IllegalArgumentException if the listener is null
	 * 
	 * @see DisposeListener
	 * @see #removeDisposeListener
	 */
	void addDisposeListener(DisposeListener listener);
	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when the interface is disposed.
	 * 
	 * @param listener
	 *            the listener which should no longer be notified when the
	 *            receiver is disposed
	 * 
	 * @exception IllegalArgumentException if the listener is null
	 * 
	 * @see DisposeListener
	 * @see #addDisposeListener
	 */
	void removeDisposeListener(DisposeListener listener);
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
	boolean isDisposed();
	/**
	 * Disposes of the operating system resources associated with the receiver
	 * and all its descendants. After this method has been invoked, the receiver
	 * and all descendants will answer <code>true</code> when sent the message
	 * <code>isDisposed()</code>. Any internal connections between the
	 * interface will have been removed to facilitate garbage collection.
	 * 
	 * @see #addDisposeListener
	 * @see #removeDisposeListener
	 */
	void dispose();
}
