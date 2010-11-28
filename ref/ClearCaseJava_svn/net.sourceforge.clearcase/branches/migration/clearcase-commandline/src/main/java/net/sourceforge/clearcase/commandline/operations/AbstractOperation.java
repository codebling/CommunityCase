package net.sourceforge.clearcase.commandline.operations;

import java.util.EnumSet;

import net.sourceforge.clearcase.OperationListener;
import net.sourceforge.clearcase.enums.Hint;
import net.sourceforge.clearcase.status.ClearCaseStatus;

public abstract class AbstractOperation implements Command {

	private String comment;
	private String[] elements;

	private EnumSet<Hint> flags;

	private OperationListener operationListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.operations.Operation#execute(java
	 * .lang.String[], java.lang.String, java.util.EnumSet,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public final ClearCaseStatus execute() {
		validateInput();
		final String[] output = executeCommand();
		final ClearCaseStatus status = parseOutput(output);
		return status;
	}

	/**
	 * Execute the actual cleartool command mapped to the API.
	 * 
	 */
	protected abstract String[] executeCommand();

	public String getComment() {
		return comment;
	}

	public String[] getElements() {
		return elements;
	}

	public EnumSet<Hint> getFlags() {
		return flags;
	}

	public OperationListener getOperationListener() {
		return operationListener;
	}

	/**
	 * Parses the output from cleartool.
	 * 
	 * @param output
	 *            The output from cleartool
	 * @return The status resulting from parsing
	 */
	protected abstract ClearCaseStatus parseOutput(String[] output);

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public void setElements(final String[] elements) {
		this.elements = elements;
	}

	public void setFlags(final EnumSet<Hint> flags) {
		this.flags = flags;
	}

	public void setOperationListener(final OperationListener operationListener) {
		this.operationListener = operationListener;
	}

	/**
	 * This method is meant to validate the input for the given command. If any
	 * input is invalid, an IllegalArgumentException must be thrown with a
	 * message specifying the nature of the illegal input.
	 * 
	 * @param elements
	 * @param comment
	 * @param flags
	 * @param operationListener
	 */
	protected abstract void validateInput();
}
