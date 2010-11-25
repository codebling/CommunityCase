package net.sourceforge.clearcase.status;

import java.util.ArrayList;
import java.util.Collection;


/**
 * A ClearCaseStatus is returned by any ClearCase operation. It consists into 3 sections
 * <ul>
 * 	<li>states : describe the states of elements after applying the operation</li>
 * 	<li>warnings : warnings that the operation may have raised. Usually informational</li>
 * 	<li>errors : errors that the operation raised. This usually means that the operation didn't go as expected. </li>
 * </ul>
 */
public class ClearCaseStatus {
	private final Collection<ClearCaseElementState> states = new ArrayList<ClearCaseElementState>();
	private final Collection<ClearCaseWarning> warnings = new ArrayList<ClearCaseWarning>();
	private final Collection<ClearCaseError> errors = new ArrayList<ClearCaseError>();
	
	public ClearCaseElementState[] getStates() {
		return (ClearCaseElementState[])states.toArray(new ClearCaseElementState[states.size()]);
	}
	public ClearCaseWarning[] getWarnings() {
		return (ClearCaseWarning[])warnings.toArray(new ClearCaseWarning[warnings.size()]);
	}
	public ClearCaseError[] getErrors() {
		return (ClearCaseError[])errors.toArray(new ClearCaseError[errors.size()]);
	}
	
	public void addState(ClearCaseElementState state) {
		states.add(state);
	}
	public void addWarning(ClearCaseWarning warning) {
		warnings.add(warning);
	}
	public void addError(ClearCaseError error) {
		errors.add(error);
	}
}
