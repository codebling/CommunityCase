package net.sourceforge.clearcase.exceptions;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseException;

/**
 * This exception is thrown whenever an operation is called after the interface
 * has been disposed.
 */
public class InterfaceDisposedException extends ClearCaseException {

	private static final long serialVersionUID = 6812257827265253384L;
	
	private final ClearCase disposedInterface;

	public ClearCase getDisposedInterface() {
		return disposedInterface;
	}

	public InterfaceDisposedException(final ClearCase disposedInterface) {
		super();
		this.disposedInterface = disposedInterface;
	}

	@Override
	public String getMessage() {
		return "The interface "+ disposedInterface.getName() +" has been disposed";
	}

}
