package net.sourceforge.clearcase.commandline.impl;

import java.util.EnumSet;

import net.sourceforge.clearcase.OperationListener;
import net.sourceforge.clearcase.commandline.operations.Add;
import net.sourceforge.clearcase.commandline.operations.Checkin;
import net.sourceforge.clearcase.commandline.operations.Checkout;
import net.sourceforge.clearcase.commandline.operations.Delete;
import net.sourceforge.clearcase.commandline.operations.FindCheckouts;
import net.sourceforge.clearcase.commandline.operations.GetElementStates;
import net.sourceforge.clearcase.commandline.operations.Move;
import net.sourceforge.clearcase.commandline.operations.Uncheckout;
import net.sourceforge.clearcase.commandline.operations.Update;
import net.sourceforge.clearcase.enums.Hint;
import net.sourceforge.clearcase.impl.ClearCaseDefaultImpl;
import net.sourceforge.clearcase.status.ClearCaseStatus;

public class ClearCaseCLIImpl extends ClearCaseDefaultImpl {

	@Override
	public ClearCaseStatus add(final String element, final String comment,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return add(new String[] { element }, comment, flags, operationListener);
	}

	@Override
	public ClearCaseStatus add(final String[] elements, final String comment,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		final Add command = new Add();
		command.setElements(elements);
		command.setComment(comment);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus checkin(final String element, final String comment,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return checkin(new String[] { element }, comment, flags,
				operationListener);
	}

	@Override
	public ClearCaseStatus checkin(final String[] elements,
			final String comment, final EnumSet<Hint> flags,
			final OperationListener operationListener) {
		final Checkin command = new Checkin();
		command.setElements(elements);
		command.setComment(comment);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus checkout(final String element, final String comment,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return checkout(new String[] { element }, comment, flags,
				operationListener);
	}

	@Override
	public ClearCaseStatus checkout(final String[] elements,
			final String comment, final EnumSet<Hint> flags,
			final OperationListener operationListener) {
		final Checkout command = new Checkout();
		command.setElements(elements);
		command.setComment(comment);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus delete(final String element, final String comment,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return delete(new String[] { element }, comment, flags,
				operationListener);
	}

	@Override
	public ClearCaseStatus delete(final String[] elements,
			final String comment, final EnumSet<Hint> flags,
			final OperationListener operationListener) {
		final Delete command = new Delete();
		command.setElements(elements);
		command.setComment(comment);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus findCheckouts(final String[] elements,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		final FindCheckouts command = new FindCheckouts();
		command.setElements(elements);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus getElementState(final String element,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return getElementStates(new String[] { element }, flags,
				operationListener);
	}

	@Override
	public ClearCaseStatus getElementStates(final String[] elements,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		final GetElementStates command = new GetElementStates();
		command.setElements(elements);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public String getName() {
		return "ClearCase Command Line";
	}

	@Override
	public ClearCaseStatus move(final String element, final String target,
			final String comment, final EnumSet<Hint> flags,
			final OperationListener operationListener) {
		final Move command = new Move();
		command.setElements(new String[] { element, target });
		command.setComment(comment);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public ClearCaseStatus uncheckout(final String element,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		return uncheckout(new String[] { element }, flags, operationListener);
	}

	@Override
	public ClearCaseStatus uncheckout(final String[] elements,
			final EnumSet<Hint> flags, final OperationListener operationListener) {
		final Uncheckout command = new Uncheckout();
		command.setElements(elements);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		return command.execute();
	}

	@Override
	public void update(final String[] elements, final EnumSet<Hint> flags,
			final OperationListener operationListener) {
		final Update command = new Update();
		command.setElements(elements);
		command.setFlags(flags);
		command.setOperationListener(operationListener);
		command.execute();
	}

}
