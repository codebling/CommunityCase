package net.sourceforge.clearcase.exceptions;

import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.clearcase.status.ClearCaseElementState;

/**
 * This exception is thrown whenever an operation has been canceled.
 */
public class OperationCanceledException extends ClearCaseException {

	private static final long serialVersionUID = -8194063161479576572L;

	private final ClearCaseElementState[] ccElementStates;

	/**
	 * @return an array of ClearCase elements already updated by the operation
	 *         before cancellation.
	 */
	public ClearCaseElementState[] getCcElementStates() {
		return ccElementStates;
	}

	public OperationCanceledException(
			final ClearCaseElementState[] ccElementStates) {
		super();
		this.ccElementStates = ccElementStates;
	}

	public OperationCanceledException(final Throwable cause,
			final ClearCaseElementState[] ccElementStates) {
		super(cause);
		this.ccElementStates = ccElementStates;
	}

	@Override
	public String getMessage() {
		return "The operation was cancelled by the user.";
	}

}
