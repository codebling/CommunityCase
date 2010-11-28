package net.sourceforge.clearcase.impl;

import java.util.EnumSet;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.DisposeListener;
import net.sourceforge.clearcase.OperationListener;
import net.sourceforge.clearcase.enums.Hint;
import net.sourceforge.clearcase.enums.ViewType;
import net.sourceforge.clearcase.status.ClearCaseStatus;


/**
 * This is the default ClearCase implementation. It is meant to be subclassed,
 * and child classes are free to implement any operation.
 * By default, every operation throws an UnsupportedOperationException.
 */
public abstract class ClearCaseDefaultImpl implements ClearCase {

	public ClearCaseStatus add(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus add(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus checkin(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus checkin(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus checkout(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus checkout(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus delete(String element, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus delete(String[] elements, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus findCheckouts(String[] elements,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus getElementState(String element, EnumSet<Hint> flags,
			OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus getElementStates(String[] elements,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public abstract String getName();

	public String getViewConfigSpec(String viewName) {
		throw new UnsupportedOperationException();
	}

	public ViewType getViewFlags(String viewName) {
		throw new UnsupportedOperationException();
	}

	public String getViewLocation(String viewName) {
		throw new UnsupportedOperationException();
	}

	public String getViewName(String element) {
		throw new UnsupportedOperationException();
	}

	public String[] getViewNames() {
		throw new UnsupportedOperationException();
	}

	public String[] getVobNames(OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public boolean isDifferent(String element) {
		throw new UnsupportedOperationException();
	}

	public String[] listAllVersionsOfElement(String element) {
		throw new UnsupportedOperationException();
	}

	public void merge(String element) {
		throw new UnsupportedOperationException();
	}

	public void mountVob(String vobName) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus move(String element, String target, String comment,
			EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public void setViewConfigSpec(String viewName, String configSpecFile) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus uncheckout(String element, EnumSet<Hint> flags,
			OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public ClearCaseStatus uncheckout(String[] elements, EnumSet<Hint> flags,
			OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public void unmountVob(String vobName) {
		throw new UnsupportedOperationException();
	}

	public void update(String[] elements, EnumSet<Hint> flags, OperationListener operationListener) {
		throw new UnsupportedOperationException();
	}

	public void addDisposeListener(DisposeListener listener) {
		throw new UnsupportedOperationException();
	}

	public void dispose() {
		throw new UnsupportedOperationException();
	}

	public boolean isDisposed() {
		throw new UnsupportedOperationException();
	}

	public void removeDisposeListener(DisposeListener listener) {
		throw new UnsupportedOperationException();
	}

}
