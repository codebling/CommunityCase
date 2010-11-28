package net.sourceforge.clearcase.exceptions;

import net.sourceforge.clearcase.ClearCaseException;


/**
 * This exception is thrown whenever a file is declared as not accessible by the
 * ClearCase client.
 */
public class NotAccessibleException extends ClearCaseException {

	private static final long serialVersionUID = 8218111165768408718L;

	/**
	 * The element which is not accessible.
	 */
	private String element;

	public NotAccessibleException(String element) {
		super();
		this.element = element;
	}

	/**
	 * @return The element declared as not accessible by ClearCase.
	 */
	public String getElement() {
		return element;
	}

	@Override
	public String getMessage() {
		return "The element " + element + " is not accessible";
	}
}
