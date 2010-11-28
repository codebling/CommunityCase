package net.sourceforge.clearcase;

import java.util.EnumSet;

import net.sourceforge.clearcase.enums.Hint;
import net.sourceforge.clearcase.enums.ViewType;
import net.sourceforge.clearcase.status.ClearCaseStatus;

/**
 * This interface defines the API that ClearCase implementation provides.
 * 
 * @author Vincent
 * 
 */
public interface ClearCase extends Disposable {

	/**
	 * <p>
	 * Adds the specified element to ClearCase source control.
	 * </p>
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>FORCE</code> hint is set the parent element will be added if
	 * necessary and checked out, otherwise it must be checked out before
	 * calling this method. The <code>CHECKIN</code> may be used to check in
	 * files after adding them.
	 * </p>
	 * <p>
	 * The optional operation listener is notified about any progress and is
	 * asked if the operation should be canceled.
	 * </p>
	 * 
	 * @param element
	 *            the element to check in
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
	 *                <li>ERROR_NULL_ARGUMENT - if the element is null</li>
	 *                </ul>
	 * @see Hint#CHECKIN
	 * @see Hint#DIR
	 * @see Hint#FORCE
	 * @see Hint#MASTER
	 * @see Hint#NO_CHECK_OUT
	 * @see Hint#PTIME
	 */
	ClearCaseStatus add(String element, String comment, EnumSet<Hint> flags,
			OperationListener operationListener);

	/**
	 * <p>
	 * Adds the specified elements to ClearCase source control.
	 * </p>
	 * <p>
	 * Comment can be empty string or <code>null</code>. If the
	 * <code>FORCE</code> hint is set the parent element will be added if
	 * necessary and checked out, otherwise it must be checked out before
	 * calling this method. The <code>CHECKIN</code> may be used to check in
	 * files after adding them.
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
	 *                <li>ERROR_NULL_ARGUMENT - if one of the elements is null</li>
	 *                </ul>
	 * 
	 * @see Hint#CHECKIN
	 * @see Hint#DIR
	 * @see Hint#FORCE
	 * @see Hint#MASTER
	 * @see Hint#NO_CHECK_OUT
	 * @see Hint#PTIME
	 */
	ClearCaseStatus add(String[] elements, String comment, EnumSet<Hint> flags,
			OperationListener operationListener);

	ClearCaseStatus checkin(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	ClearCaseStatus checkin(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	ClearCaseStatus checkout(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	ClearCaseStatus checkout(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	ClearCaseStatus delete(String element, String comment, EnumSet<Hint> flags,
			OperationListener operationListener);

	ClearCaseStatus delete(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	ClearCaseStatus findCheckouts(String[] elements, EnumSet<Hint> flags,
			OperationListener operationListener);

	ClearCaseStatus getElementState(String element, EnumSet<Hint> flags,
			OperationListener operationListener);

	ClearCaseStatus getElementStates(String[] elements, EnumSet<Hint> flags,
			OperationListener operationListener);

	String getName();

	String getViewConfigSpec(String viewName);

	ViewType getViewFlags(String viewName);

	String getViewLocation(String viewName);

	String getViewName(String element);

	String[] getViewNames();

	String[] getVobNames(OperationListener operationListener);

	boolean isDifferent(String element);

	String[] listAllVersionsOfElement(String element);

	void merge(String element);

	void mountVob(String vobName);

	ClearCaseStatus move(String element, String target, String comment,
			EnumSet<Hint> flags, OperationListener operationListener);

	void setViewConfigSpec(String viewName, String configSpecFile);

	ClearCaseStatus uncheckout(String element, EnumSet<Hint> flags,
			OperationListener operationListener);

	ClearCaseStatus uncheckout(String[] elements, EnumSet<Hint> flags,
			OperationListener operationListener);

	void unmountVob(String vobName);

	void update(String[] elements, EnumSet<Hint> flags,
			OperationListener operationListener);
}
