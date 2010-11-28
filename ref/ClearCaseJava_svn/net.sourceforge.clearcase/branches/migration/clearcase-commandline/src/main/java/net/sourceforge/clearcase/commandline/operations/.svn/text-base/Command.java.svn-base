package net.sourceforge.clearcase.commandline.operations;

import net.sourceforge.clearcase.status.ClearCaseStatus;

public interface Command {

	/**
	 * Executes an operation. An operation is made of three steps
	 * <ul>
	 * 	<li>validation of the input</li>
	 *  <li>execution of the cleartool command</li>
	 *  <li>parsing of the output</li>
	 * @param elements
	 * @param comment
	 * @param flags
	 * @param operationListener
	 * @return
	 */
	public abstract ClearCaseStatus execute();

}