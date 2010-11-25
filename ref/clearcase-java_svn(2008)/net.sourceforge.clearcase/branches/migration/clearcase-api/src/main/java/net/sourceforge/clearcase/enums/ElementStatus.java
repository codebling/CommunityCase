package net.sourceforge.clearcase.enums;

/**
 * This enum is used to describe the status of a ClearCase element
 * 
 * @author Vincent
 * 
 */
public enum ElementStatus {
	/**
	 * ClearCase element state that indicates if an element is checked in.
	 */
	CHECKED_IN,
	/**
	 * ClearCase element state that indicates if an element is checked out.
	 */
	CHECKED_OUT,
	/**
	 * ClearCase element state that indicates if an element is view private.
	 */
	VIEW_PRIVATE,
	/**
	 * ClearCase element state that indicates if an element is hijacked.
	 */
	HIJACKED,
	/**
	 * ClearCase element state that indicates if an element is a link.
	 */
	LINK,
	/**
	 * ClearCase element state that indicates if an element is missing.
	 */
	MISSING,
	/**
	 * ClearCase element state that indicates if an element is checked out in
	 * another branch.
	 */
	CHECKED_OUT_IN_DIFFERENT_BRANCH,
	/**
	 * ClearCase element state that indicates if an element is checked out in
	 * another view.
	 */
	CHECKED_OUT_IN_DIFFERENT_VIEW,
	/**
	 * ClearCase element state that indicates if an element is checked out by
	 * another user.
	 */
	CHECKED_OUT_BY_DIFFERENT_USER,
	/**
	 * ClearCase element state that indicates if an element is not within a
	 * ClearCase vob
	 */
	OUTSIDE_VOB,
	/**
	 * View constant that indicates if an file or directory is an clearcase
	 * element.
	 */
	IS_ELEMENT,
	/**
	 * View constant that indicates if an file or directory is removed eg no
	 * longer an element.
	 */
	REMOVED,
	/**
	 * ClearCase element state that indicates if an element has been moved
	 * within a ClearCase vob
	 */
	MOVED
}
