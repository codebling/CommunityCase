package net.sourceforge.clearcase.enums;

public enum Hint {
	/**
	 * Operation constant for performing a check in immediately after the
	 * operation.
	 */
	CHECKIN("-ci"),
	/**
	 * Operation content for listing only directory itself, not its content.
	 */
	DIR,
	/**
	 * Operation constant for forcing operations.
	 */
	FORCE("-force"),
	/**
	 * Operation constant for performing the merge graphically.
	 */
	GRAPHICAL("-graphical"),
	/**
	 * Operation constant for performing a check in even if the version is
	 * identical to its predecessor.
	 */
	IDENTICAL("-identical"),
	/**
	 * Operation constant for keeping a backup on undo checkouts.
	 */
	KEEP("-keep"),
	/**
	 * Operation constant for making an element the master of all replicas.
	 */
	MASTER("-master"),
	/**
	 * Operation constant for performing an automatic merge on check in if
	 * necessary.
	 */
	MERGE,
	/**
	 * Operation constant for suppressing checkout of the new directory element.
	 */
	NO_CHECK_OUT("-nco"),
	/**
	 * Operation constant for preserving times.
	 */
	PTIME("-ptime"),
	/**
	 * Operation constant for working recursively.
	 */
	RECURSIVE("-recurse"),
	/**
	 * ClearCase element state and operation constant that indicates if a
	 * checked out is/must be reserved.
	 */
	RESERVED("-reserved"),
	/**
	 * ClearCase element state and operation constant that indicates if a
	 * checked out is/must be unreserved.
	 */
	UNRESERVED("-unreserved");

	private String option;

	private Hint() {
	}

	private Hint(final String option) {
		this.option = option;
	}

	public String getOption() {
		return this.option;
	}
}
