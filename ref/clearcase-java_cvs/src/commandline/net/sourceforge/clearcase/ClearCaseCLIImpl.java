/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - API rework, feature enhancements, bug fixes
 *     Vincent Latombe - refactoring
 *******************************************************************************/
package net.sourceforge.clearcase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
import net.sourceforge.clearcase.commandline.CommandLauncher;
import net.sourceforge.clearcase.commandline.CommandLine;
import net.sourceforge.clearcase.commandline.ICommandLauncher;
import net.sourceforge.clearcase.commandline.Response;
import net.sourceforge.clearcase.commandline.SingleProcessCommandLauncher;
import net.sourceforge.clearcase.commandline.output.MatchStrategyComposite;
import net.sourceforge.clearcase.commandline.output.strategy.DirectoryIsNotCheckedOut;
import net.sourceforge.clearcase.commandline.output.strategy.ElementAlreadyCheckedout;
import net.sourceforge.clearcase.commandline.output.strategy.ElementAlreadyExists;
import net.sourceforge.clearcase.commandline.output.strategy.ElementCheckedOutReservedInOtherView;
import net.sourceforge.clearcase.commandline.output.strategy.ElementHasCheckOuts;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsCheckedIn;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsCheckedout;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsDerivedObject;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsHijacked;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsLink;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsReservedCheckout;
import net.sourceforge.clearcase.commandline.output.strategy.ElementIsUnReservedCheckout;
import net.sourceforge.clearcase.commandline.output.strategy.IdenticalPredecessor;
import net.sourceforge.clearcase.commandline.output.strategy.MostRecentNotPredecessor;
import net.sourceforge.clearcase.commandline.output.strategy.NotSupportedInSnapshotView;
import net.sourceforge.clearcase.commandline.output.strategy.ObjectNotInsideVob;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulAdd;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulCheckin;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulCheckout;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulDelete;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulMerge;
import net.sourceforge.clearcase.commandline.output.strategy.SuccessfulMove;
import net.sourceforge.clearcase.commandline.output.strategy.UnableToAccessElement;
import net.sourceforge.clearcase.commandline.output.strategy.UnableToCreateElement;
import net.sourceforge.clearcase.commandline.output.strategy.ViewPrivateObject;
import net.sourceforge.clearcase.events.OperationListener;

/**
 * This class is the command line interface for ClearCase. It performs every
 * ClearCase operation using the "cleartool" command.
 * <p>
 * This class cannot be instantiated directly. The <code>ClearCase</code>
 * factory must be used instead.
 * </p>
 * 
 * @see ClearCase#createInterface(int)
 */
public class ClearCaseCLIImpl extends ClearCaseInterface {

	/**
	 * field <code>TIMEOUT_GRAPHICAL_TOOLS</code>, number of seconds to wait for
	 * errors when launching graphical tools
	 */
	private static final int TIMEOUT_GRAPHICAL_TOOLS = 2;

	private boolean useSingleprocessLauncher = false;

	private static int debugLevel = 0;

	public boolean isUseSingleprocessLauncher() {
		return useSingleprocessLauncher;
	}

	public void setSingleprocessLauncher(boolean useSingleprocessLauncher) {
		this.useSingleprocessLauncher = useSingleprocessLauncher;
	}

	MatchStrategyComposite matchComposite = null;

	/**
	 * A custom CleartoolCommandLine implementation that reads the cleartool
	 * executable from the interface configuration.
	 */
	public final class CleartoolCLICommandLine extends CleartoolCommandLine {

		private boolean needsWorkingDirectory = false;

		/**
		 * Returns the needsWorkingDirectory.
		 * 
		 * @return returns the needsWorkingDirectory
		 */
		public boolean needsWorkingDirectory() {
			return needsWorkingDirectory;
		}

		/**
		 * Sets the value of needsWorkingDirectory.
		 * 
		 * @param needsWorkingDirectory
		 *            the needsWorkingDirectory to set
		 */
		public void setNeedsWorkingDirectory(boolean needsWorkingDirectory) {
			this.needsWorkingDirectory = needsWorkingDirectory;
		}

		/**
		 * Creates a new instance.
		 * 
		 * @param baseCmd
		 *            the base command
		 */
		public CleartoolCLICommandLine(final String baseCmd) {
			super(baseCmd);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @seenet.sourceforge.clearcase.commandline.CleartoolCommandLine#
		 * getCleartoolExecutable()
		 */
		protected String getExecutable() {
			// lookup the cleartool command in the configuration
			return (String) getData(ClearCaseCLIImpl.CLEARTOOL_EXECUTABLE);
		}
	}

	/**
	 * A custom environment that is used for launching the "cleartool"
	 * executable. The value is expected to be of type <code>String[]</code>.
	 * See
	 * {@link Runtime#exec(java.lang.String, java.lang.String[], java.io.File)}
	 * for details.
	 * <p>
	 * The default is <code>null</code>, which inherits the parent environment.
	 * </p>
	 */
	public static final Object CLEARTOOL_ENVIRONMENT = new Object();

	/**
	 * The "cleartool" executable. The value is expected to be of type
	 * <code>String</code>.
	 * <p>
	 * If the <code>cleartool</code> executable is not located within the
	 * system's search path, you need to submit the complete binary path.
	 * </p>
	 */
	public static final Object CLEARTOOL_EXECUTABLE = new Object();

	/** the logger. */
	private static final Logger LOG = Logger.getLogger(ClearCaseCLIImpl.class
			.getName());

	/**
	 * The specified element is checked in.
	 * 
	 * @param element
	 * @return an element state
	 */
	static ClearCaseElementState checkedIn(final String element) {
		return new ClearCaseElementState(element, ClearCase.CHECKED_IN);
	}

	/**
	 * The specified element is checked in.
	 * 
	 * @param element
	 * @param version
	 * @return an element state
	 */
	static ClearCaseElementState checkedIn(final String element,
			final String version) {
		return new ClearCaseElementState(element, ClearCase.CHECKED_IN, version);
	}

	/**
	 * The specified element is checked out.
	 * 
	 * @param element
	 * @return an element state
	 */
	static ClearCaseElementState checkedOut(final String element) {
		return new ClearCaseElementState(element, ClearCase.CHECKED_OUT);
	}

	/**
	 * The specified element is checked out.
	 * 
	 * @param element
	 * @param version
	 * @return an element state
	 */
	static ClearCaseElementState checkedOut(final String element,
			final String version) {
		return new ClearCaseElementState(element, ClearCase.CHECKED_OUT,
				version);
	}

	/**
	 * Generates an IO error if an expected result is empty.
	 * 
	 * @throws ClearCaseError
	 *             <code>ERROR_IO</code>
	 */
	static void errorEmptyResult() {
		ClearCase.error(ClearCase.ERROR_IO, "no result from cleartool"); //$NON-NLS-1$
	}

	/**
	 * Generates an IO error if a result could not be interpreted.
	 * 
	 * @throws ClearCaseError
	 *             <code>ERROR_IO</code>
	 */
	static void errorUnknownResult() {
		ClearCase.error(ClearCase.ERROR_IO,
				"could not interpret cleartool result"); //$NON-NLS-1$
	}

	/**
	 * The specified element is already checked out.
	 * 
	 * @param element
	 *            the filename
	 * @return an element state
	 */
	static ClearCaseElementState isAlreadyCheckedOut(final String element) {
		return new ClearCaseElementState(element,
				ClearCase.ERROR_ALREADY_CHECKED_OUT);
	}

	/**
	 * The specified element is checked in.
	 * 
	 * @param element
	 *            the filename
	 * @return an element state
	 */
	static ClearCaseElementState isElement(final String element) {
		return new ClearCaseElementState(element, ClearCase.IS_ELEMENT);
	}

	/**
	 * The specified element is removed.
	 * 
	 * @param element
	 *            the filename
	 * @return an element state
	 */
	static ClearCaseElementState isRemoved(final String element) {
		return new ClearCaseElementState(element, ClearCase.REMOVED);
	}

	/**
	 * The specified element is view private.
	 * 
	 * @param element
	 *            the filename
	 * @return an element state
	 */
	static ClearCaseElementState isViewPrivate(final String element) {
		return new ClearCaseElementState(element, ClearCase.VIEW_PRIVATE);
	}

	/**
	 * The specified element is a link.
	 * 
	 * @param element
	 *            the filename
	 * @param linkTarget
	 *            the filename of the link target
	 * @return an element state
	 */
	static ClearCaseElementState link(final String element,
			final String linkTarget) {
		return new ClearCaseElementState(element, ClearCase.LINK, null,
				linkTarget);
	}

	/**
	 * The operation was canceled.
	 * 
	 * @throws ClearCaseException
	 *             <code>ERROR_OPERATION_CANCELED</code>
	 */
	static void operationCanceled() {
		ClearCase.error(ClearCase.ERROR_OPERATION_CANCELED);
	}

	/**
	 * The specified element is not within a vob.
	 * 
	 * @param element
	 *            the filename
	 * @return an element state
	 */
	static ClearCaseElementState outsideVob(final String element) {
		return new ClearCaseElementState(element, ClearCase.OUTSIDE_VOB);
	}

	/**
	 * The specified element is a view private element.
	 * 
	 * @param element
	 *            the filename
	 * @return the element state
	 */
	static ClearCaseElementState viewPrivate(final String element) {
		return new ClearCaseElementState(element, ClearCase.VIEW_PRIVATE);
	}

	/**
	 * Creates a new instance.
	 * <p>
	 * This constructor is package visible so that it can only be called from
	 * <code>ClearCase</code>.
	 * </p>
	 */
	ClearCaseCLIImpl() {
		// configure defaults
		setData(CLEARTOOL_EXECUTABLE, "cleartool"); //$NON-NLS-1$
		matchComposite = new MatchStrategyComposite();
		setupStrategies();

	}

	/**
	 * Add classes that handles the output from clearcase. Order of patterns is
	 * important since matching is done in a sequence ( as strategies are
	 * added).
	 */
	protected void setupStrategies() {
		matchComposite.addStrategy(new ElementIsCheckedIn());
		matchComposite.addStrategy(new ElementIsCheckedout());
		matchComposite.addStrategy(new ElementIsLink());
		matchComposite.addStrategy(new ElementIsHijacked());
		matchComposite.addStrategy(new ElementIsDerivedObject());
		matchComposite.addStrategy(new ElementIsUnReservedCheckout());
		matchComposite.addStrategy(new ElementIsReservedCheckout());
		matchComposite.addStrategy(new SuccessfulCheckin());
		matchComposite.addStrategy(new SuccessfulCheckout());
		matchComposite.addStrategy(new SuccessfulAdd());
		matchComposite.addStrategy(new SuccessfulMerge());
		matchComposite.addStrategy(new ElementAlreadyCheckedout());
		matchComposite.addStrategy(new DirectoryIsNotCheckedOut());
		matchComposite.addStrategy(new IdenticalPredecessor());
		matchComposite.addStrategy(new UnableToAccessElement());
		matchComposite.addStrategy(new ObjectNotInsideVob());

		matchComposite.addStrategy(new MostRecentNotPredecessor());
		matchComposite.addStrategy(new ElementAlreadyExists());

		matchComposite.addStrategy(new SuccessfulDelete());
		matchComposite.addStrategy(new SuccessfulMove());
		matchComposite.addStrategy(new ElementHasCheckOuts());

		matchComposite.addStrategy(new ElementCheckedOutReservedInOtherView());
		matchComposite.addStrategy(new UnableToCreateElement());
		matchComposite.addStrategy(new NotSupportedInSnapshotView());

		matchComposite.addStrategy(new ViewPrivateObject());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.ClearCaseInterface#add(java.lang.String,
	 * java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState add(final String element,
			final boolean isDirectory, final String comment, final int flags,
			final OperationListener operationListener) {
		String[] elements = null;
		ClearCaseElementState[] result = null;

		if (element != null) {
			elements = new String[] { element };
		}
		final List<String> optionsList = new ArrayList<String>();
		// is comment specified
		if (null == comment || "".equals(comment.trim())) {
			optionsList.add("-nc"); //$NON-NLS-1$
		} else {
			optionsList.add("-c");
			optionsList.add(Utils.escapeComment(comment));
		}

		// preserve time
		if (isSet(flags, ClearCase.PTIME)) {
			optionsList.add("-pti");
		}

		// checkin
		if (isSet(flags, ClearCase.CHECKIN)) {
			optionsList.add("-ci"); //$NON-NLS-1$
		}

		if (isSet(flags, ClearCase.NO_CHECK_OUT)) {
			optionsList.add("-nco");
		}

		// master
		if (isSet(flags, ClearCase.MASTER)) {
			optionsList.add("-master"); //$NON-NLS-1$
		}

		final String[] options = Utils.toArray(optionsList);
		String operation = null;
		if (isDirectory) {
			operation = "mkdir";
		} else {
			operation = "mkelem";

		}

		try {
			result = ccOperation(operation, options, elements, comment,
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_CAN_NOT_CREATE_ELEMENT:
				ClearCase.error(ClearCase.ERROR_CAN_NOT_CREATE_ELEMENT);
				break;
			case ClearCase.ERROR_ALREADY_EXISTS:
				ClearCase.error(ClearCase.ERROR_ALREADY_EXISTS, cce, result);
			case ClearCase.ERROR_DIR_IS_NOT_CHECKED_OUT:
				ClearCase.error(ClearCase.ERROR_DIR_IS_NOT_CHECKED_OUT, cce,
						result);
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		return result[0];
	}

	/**
	 * 
	 * @param element
	 * @param group
	 * @param operationListener
	 * @return
	 */
	public ClearCaseElementState setGroup(String element, String group,
			final OperationListener operationListener) {

		String[] elements = null;
		ClearCaseElementState[] result = null;

		if (element != null) {
			elements = new String[] { element };
		}
		final List<String> optionsList = new ArrayList<String>();

		optionsList.add("-chgrp"); //$NON-NLS-1$
		optionsList.add(group);

		final String[] options = Utils.toArray(optionsList);
		String operation = "protect";

		try {
			result = ccOperation(operation, options, elements, "",
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_CAN_NOT_CREATE_ELEMENT:
				ClearCase.error(ClearCase.ERROR_CAN_NOT_CREATE_ELEMENT);
				break;
			case ClearCase.ERROR_ALREADY_EXISTS:
				ClearCase.error(ClearCase.ERROR_ALREADY_EXISTS, cce, result);
			case ClearCase.ERROR_DIR_IS_NOT_CHECKED_OUT:
				ClearCase.error(ClearCase.ERROR_DIR_IS_NOT_CHECKED_OUT, cce,
						result);
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		return result[0];
	}

	// /**
	// *
	// * @param input
	// * @return
	// */
	// private ClearCaseElementState[] reduceResults(
	// final ClearCaseElementState[] input) {
	// ClearCaseElementState[] output = null;
	// final Map<String, ClearCaseElementState> elements = new HashMap<String,
	// ClearCaseElementState>();
	// for (int i = 0; i < input.length; i++) {
	// elements.put(input[i].element, input[i]);
	// }
	// final Collection values = elements.values();
	// output = new ClearCaseElementState[values.size()];
	// int i = 0;
	// for (final Iterator iter = values.iterator(); iter.hasNext();) {
	// output[i++] = (ClearCaseElementState) iter.next();
	// }
	// return output;
	// }

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// net.sourceforge.clearcase.ClearCaseInterface#ccElement(java.lang.String,
	// * int, net.sourceforge.clearcase.events.OperationListener)
	// */
	// public ClearCaseElementState ccElement(final String element,
	// final int flags, final OperationListener operationListener) {
	// /*
	// * TODO : Check if this method is useful : what do we expect compared to
	// * getElementState?
	// */
	// final ClearCaseElementState[] result = clearcaseOperation("describe",
	// new String[] { "-fmt \"%m\"" }, new String[] { element }, null,
	// operationListener);
	// if (result.length == 0) {
	// return null;
	// } else if (result.length > 1) {
	// LOG.warning("ccElement: got " + result.length + " instead of 1. "
	// + result);
	// }
	// return result[0];
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#checkin(java.lang.String[],
	 * java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] checkin(final String[] elements,
			final String comment, final int flags,
			final OperationListener operationListener) {
		ClearCaseElementState[] result = null;

		final List<String> optionsList = new ArrayList<String>();
		optionsList.add("-nwa");
		if (null == comment || "".equals(comment.trim())) {
			optionsList.add("-nc"); //$NON-NLS-1$
		} else {
			optionsList.add("-c");
			optionsList.add(Utils.escapeComment(comment));
		}

		// preserve time
		if (isSet(flags, ClearCase.PTIME)) {
			optionsList.add("-pti"); //$NON-NLS-1$
		}

		// identical
		if (isSet(flags, ClearCase.FORCE) || isSet(flags, ClearCase.IDENTICAL)) {
			optionsList.add("-ide"); //$NON-NLS-1$
		}

		final String[] options = Utils.toArray(optionsList);
		try {
			result = ccOperation("ci", options, elements, null,
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL:
				ClearCase.error(ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL);
				break;
			case ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION:
				ClearCase
						.error(ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION);
				break;
			case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
				ClearCase.error(ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS);
				break;
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#checkout(java.lang.String[],
	 * java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] checkout(final String[] elements,
			final String comment, final int flags,
			final OperationListener operationListener) {
		final List<String> optionsList = new ArrayList<String>();
		ClearCaseElementState[] result = null;
		optionsList.add("-nwa");

		// is comment specified
		if (null == comment || comment.equals("")) {
			optionsList.add("-nc"); //$NON-NLS-1$
		} else {
			optionsList.add("-c");
			optionsList.add(Utils.escapeComment(comment));
		}

		// preserve time
		if (isSet(flags, ClearCase.PTIME)) {
			optionsList.add("-pti"); //$NON-NLS-1$
		}

		// reservered, unreservered or both
		if (isSet(flags, ClearCase.RESERVED_IF_POSSIBLE)) {
			optionsList.add("-res"); //$NON-NLS-1$
			optionsList.add("-unr"); //$NON-NLS-1$
		} else {
			if (isSet(flags, ClearCase.RESERVED)) {
				optionsList.add("-res"); //$NON-NLS-1$
			}
			if (isSet(flags, ClearCase.UNRESERVED)) {
				optionsList.add("-unr"); //$NON-NLS-1$
			}
		}
		if (isSet(flags, ClearCase.HIJACKED)) {
			optionsList.add("-usehijack"); //$NON-NLS-1$
		}

		final String[] options = Utils.toArray(optionsList);

		try {
			result = ccOperation("co", options, elements, null,
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_ALREADY_CHECKED_OUT:
				ClearCase.error(ClearCase.ERROR_ALREADY_CHECKED_OUT);
			case ClearCase.ERROR_NOT_AN_ELEMENT:
				ClearCase.error(ClearCase.ERROR_NOT_AN_ELEMENT);
			case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
				ClearCase.error(ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS);
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		return result;

	}

	protected ClearCaseElementState[] ccOperation(final String command,
			final String[] options, final String[] elements,
			final String comment, final OperationListener operationListener)
			throws ClearCaseException {
		// check
		if (null == elements) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"elements must not be null"); //$NON-NLS-1$
		}
		checkInterface();

		String[] output = buildAndExecuteCommand(command, options, elements,
				operationListener);
		ClearCaseElementState[] states;
		if (output != null) {
			// process command output
			states = parserCCOutput(output);
		} else {
			states = new ClearCaseElementState[0];
		}
		if (null != operationListener) {
			operationListener.finishedOperation();
		}

		// return result
		return states;
	}

	private String[] buildAndExecuteCommand(final String command,
			final String[] options, final String[] elements,
			final OperationListener operationListener) {
		return buildAndExecuteCommand(command, options, elements, null,
				operationListener);
	}

	private String[] buildAndExecuteCommand(final String command,
			final String[] options, final String[] elements,
			final String workingdir, final OperationListener operationListener) {
		final CleartoolCLICommandLine cleartool = buildCommand(command,
				options, elements, workingdir, operationListener);

		// execute command
		final String[] output = launch(cleartool, operationListener);

		if (null != operationListener) {
			operationListener.worked(80);
		}

		return output;
	}

	/**
	 * @param command
	 * @param options
	 * @param elements
	 * @param workingdir
	 * @param operationListener
	 * @return
	 */
	private CleartoolCLICommandLine buildCommand(final String command,
			final String[] options, final String[] elements,
			final String workingdir, final OperationListener operationListener) {
		final CleartoolCLICommandLine cleartool = new CleartoolCLICommandLine(
				command);
		cleartool.setElements(elements);
		for (int i = 0; i < options.length; i++) {
			cleartool.addOption(options[i]);
		}

		if (null != operationListener) {
			operationListener.startedOperation(100);
		}
		if (workingdir != null) {
			cleartool.setNeedsWorkingDirectory(true);
			setData(WORKING_DIR, new File(workingdir));
		}
		return cleartool;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#delete(java.lang.String[],
	 * java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] delete(final String[] elements,
			final String comment, final int flags,
			final OperationListener operationListener) {
		final List<String> optionsList = new ArrayList<String>();
		ClearCaseElementState[] result = null;

		// is comment specified
		if (null == comment) {
			optionsList.add("-nc"); //$NON-NLS-1$
		}

		// force
		if (isSet(flags, ClearCase.FORCE)) {
			optionsList.add("-f"); //$NON-NLS-1$
		}

		final String[] options = Utils.toArray(optionsList);
		// FIXME: When successfully removed we use ClearCase.REMOVED
		// Check that this is handled in gui layer.
		try {
			result = ccOperation("rmname", options, elements, comment,
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
				ClearCase.error(ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS);
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#findCheckouts(java.lang.
	 * String[], int, net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] findCheckouts(final String[] elements,
			final int flags, final OperationListener operationListener) {
		final List<String> optionsList = new ArrayList<String>();

		if (isSet(flags, ClearCase.RECURSIVE)) {
			optionsList.add("-recurse"); //$NON-NLS-1$
		} else {
			optionsList.add("-directory"); //$NON-NLS-1$
		}
		optionsList.add("-fmt");
		optionsList
				.add("\"\\\"%En\\\"\\tPredecessor: \\\"%[version_predecessor]p\\\"\\tStatus: %Rf\\n\"");
		final String[] options = Utils.toArray(optionsList);
		return ccOperation("lsco", options, elements, null, operationListener);
	}

	public String[] listAllVersionsOfElement(final String element) {
		// check
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element name must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("lsvtree")
				.addOption("element").addElement(element);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		return output;
	}

	public void merge(final String element) {
		String[] versions = listAllVersionsOfElement(element);
		if (null == versions) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"versions must not be null"); //$NON-NLS-1$
		}

		// System.out.println("Versions are: " + versions);

		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("merge")
				.addOption("-to").addOption(element);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

	}

	/**
	 * Returns the environment for the cleartool process.
	 * 
	 * @return the environment for the cleartool process (maybe
	 *         <code>null</code>)
	 */
	public String[] getCleartoolEnvironment() {
		return (String[]) getData(CLEARTOOL_ENVIRONMENT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getElementState(java.lang
	 * .String)
	 */
	public ClearCaseElementState getElementState(final String element) {
		String[] elements = null;
		if (element != null) {
			elements = new String[] { element };
		}
		final ClearCaseElementState[] result = getElementStates(elements,
				ClearCase.DIR, null);
		if (result == null || result.length == 0)
			return null;
		else if (result.length > 1) {

		}
		return result[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getElementStates(java.lang
	 * .String[], int, net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] getElementStates(final String[] elements,
			final int flags, final OperationListener operationListener) {
		final List<String> optionsList = new ArrayList<String>();
		optionsList.add("-long");
		// build options
		if (isSet(flags, ClearCase.RECURSIVE)) {
			optionsList.add("-r");
		}
		if (isSet(flags, ClearCase.DIR)) {
			optionsList.add("-d");
		}
		final String[] options = Utils.toArray(optionsList);
		return ccOperation("ls", options, elements, null, operationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.ClearCaseInterface#getName()
	 */
	public String getName() {
		return Messages.getString("ClearCaseCLIImpl.cli.name"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getViewConfigSpec(java.lang
	 * .String)
	 */
	public String getViewConfigSpec(final String viewName) {
		// check
		// System.out.println("view name is: " + viewName);
		if (null == viewName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"view name must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("catcs")
				.addOption("-tag").addOption(viewName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		// process command output
		final StringBuffer configSpec = new StringBuffer();
		for (int i = 0; i < output.length; i++) {
			configSpec.append(parserOutputCATCS(output[i] + "\n"));

		}

		return configSpec.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getViewFlags(java.lang.String
	 * )
	 */
	public int getViewFlags(final String viewName) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getViewLocation(java.lang
	 * .String)
	 */
	public String getViewLocation(final String viewName) {
		// check
		if (null == viewName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"view name must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("lsstgloc");
		cleartool.addOption("-view");
		cleartool.addElement(viewName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		// process command output
		String viewLoc = null;
		for (int i = 0; i < output.length; i++) {
			viewLoc = parserOutputLSSTGLOC(output[i]);
		}

		return viewLoc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getViewType(String viewName) {
		if (null == viewName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"view name must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("lsview");
		cleartool.addOption("-pro");
		cleartool.addOption("-ful");
		cleartool.addElement(viewName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}
		if (isError(output[0])) {
			ClearCase.error(ClearCase.ERROR_INVALID_ARGUMENT,
					"Could not get view status for " + viewName);
		}

		return parserOutputLSView(output);

	}

	// Fix for Bug 2509230.
	public ClearCaseElementState getViewType(String viewName, String element) {
		if (null == viewName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"view name must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("lsview");
		cleartool.addOption("-pro");
		cleartool.addOption("-ful");
		cleartool.addElement(viewName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}
		if (isError(output[0])) {
			ClearCase.error(ClearCase.ERROR_INVALID_ARGUMENT,
					"Could not get view status for " + viewName);
		}
		// TODO: Put in a private parser method that returns
		// ClearCaseElementState object.
		for (String row : output) {
			if (row.startsWith("Properties: snapshot"))
				return new ClearCaseElementState(element, ClearCase.SNAPSHOT);
			if (row.startsWith("Properties: dynamic"))
				return new ClearCaseElementState(element, ClearCase.DYNAMIC);
		}

		// ClearCase.error(ClearCase.ERROR_INVALID_ARGUMENT,
		// "Could not get status information from view");
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#getViewName(java.lang.String
	 * )
	 */
	public String getViewName(final String element) {
		// check
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"elements must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CleartoolCLICommandLine cleartool = new CleartoolCLICommandLine(
				"pwv");
		cleartool.addOption("-short");
		File file = new File(element);
		File dir = null;
		if (!file.isDirectory()) {
			String parent = file.getParent();
			dir = new File(parent);
		} else {
			dir = new File(element);
		}

		cleartool.setNeedsWorkingDirectory(true);
		setData(WORKING_DIR, dir);

		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		// process command output
		String viewName = null;
		for (int i = 0; i < output.length; i++) {
			viewName = parserOutputPWV(output[i]);
		}

		return viewName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.ClearCaseInterface#getViewNames()
	 */
	public String[] getViewNames() {
		final CommandLine cleartool = new CleartoolCLICommandLine("lsview")
				.addOption("-short");
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		return output;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.ClearCaseInterface#getVobNames()
	 */
	public String[] getVobNames(final OperationListener operationListener) {
		final CommandLine cleartool = new CleartoolCLICommandLine("lsvob")
				.addOption("-short");
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		if (null != operationListener) {
			operationListener.finishedOperation();
		}

		return output;
	}

	/**
	 * {@inheritDoc}
	 */
	public ClearCaseElementState[] getViewLSPrivateList(
			final String workingdir, final OperationListener operationListener) {

		// remark: lsprivate -tag xyz is not usable for our purposes, as it
		// returns stuff in view-extended notation (M:\viewname... on Windows,
		// /view/viewname/... on Unix).
		String[] output = buildAndExecuteCommand("lsprivate",
				new String[] { "-other" }, new String[] {}, workingdir,
				operationListener);

		return getElementStatus(output);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClearCaseElementState[] getViewLSViewOnlyList(String workingdir,
			OperationListener operationListener) {

		String[] output = buildAndExecuteCommand("ls", new String[] { "-r",
				"-view_only", "-nxn" }, new String[] { workingdir },
				operationListener);

		return getElementStatus(output);
	}

	@SuppressWarnings("unchecked")
	private ClearCaseElementState[] getElementStatus(String[] output) {
		List result = new ArrayList();

		if (output == null)
			return new ClearCaseElementState[0];

		if (output.length > 0 && isError(output[0])) {
			parserCCOutput(output);
		}

		for (int i = 0; i < output.length; i++) {
			String row = output[i];
			int index = row.indexOf(" ");
			if (index > -1) {
				result.add(new ClearCaseElementState(row.substring(0, index),
						ClearCase.CHECKED_OUT | ClearCase.IS_ELEMENT));
			} else {
				result.add(new ClearCaseElementState(row,
						ClearCase.VIEW_PRIVATE));
			}
		}
		return (ClearCaseElementState[]) result
				.toArray(new ClearCaseElementState[output.length]);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public ClearCaseElementState[] getCheckedOutElements(String workingdir,
			OperationListener operationListener) {
		List result = new ArrayList();

		// This is executed to ensure that the working directory is set before
		// finding checkouts
		// chdir(workingdir, operationListener);

		String[] output = buildAndExecuteCommand("lsco", new String[] { "-me",
				"-avobs", "-short", "-cview" }, null, workingdir,
				operationListener);

		if (null != output) {
			for (int i = 0; i < output.length; i++) {
				result.add(new ClearCaseElementState(output[i],
						ClearCase.CHECKED_OUT | ClearCase.IS_ELEMENT));
			}
		}

		return (ClearCaseElementState[]) result
				.toArray(new ClearCaseElementState[result.size()]);
	}

	/**
	 * @param workingdir
	 * @param operationListener
	 */
	public void chdir(String workingdir, OperationListener operationListener) {
		buildAndExecuteCommand("cd", new String[] { workingdir }, null,
				operationListener);
	}

	/**
	 * Returns the working directory for the cleartool process.
	 * 
	 * @return the working directory (maybe <code>null</code>)
	 */
	public File getWorkingDirectory() {
		return (File) getData(WORKING_DIR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#isDifferent(java.lang.String
	 * )
	 */

	// TODO: commented out for new lanuncher
	public boolean isDifferent(final String element) {
		// final ClearCaseElementState state = getElementState(element);
		// if (state.isCheckedOut()) {
		// final String[] elements = new String[] { element };
		// final CommandLine cleartool = new CleartoolCLICommandLine("diff")
		// .setElements(elements).addOption("-predecessor").addOption(
		// "-options -status_only");
		// final CommandLauncher launcher = new CommandLauncher(cleartool
		// .create(), getWorkingDirectory(),
		// getCleartoolEnvironment(), null);
		// return (launcher.getExitValue() == 1);
		// } else {
		// return false;
		// }
		return false;
	}

	/**
	 * Indicates if the result line contains an error.
	 * 
	 * @param result
	 * @return <code>true</code> if the result line contains an error
	 */
	public boolean isError(final String result) {
		return result.startsWith("cleartool: Error:"); //$NON-NLS-1$
	}

	/**
	 * Launches the specified command and notifies the specified listener about
	 * progress.
	 * 
	 * @param cleartoolCmdline
	 *            the cleartool command line
	 * @param comment
	 *            the comment to insert in interactive mode (maybe
	 *            <code>null</code> for NO comment)
	 * @param listener
	 *            the operation listener (maybe <code>null</code>)
	 * 
	 * @return the command output
	 */
	protected String[] launch(final CommandLine cleartoolCmdline,
			final OperationListener listener) {
		LOG.fine("launching: " + cleartoolCmdline); //$NON-NLS-1$
		String[] inputs = null;
		Response response = null;
		ICommandLauncher launcher = null;

		if (cleartoolCmdline.isOrdered()) {
			inputs = cleartoolCmdline.createWithCmdOrder();
		} else {
			inputs = cleartoolCmdline.create();
		}

		boolean isGraphical = false;
		boolean gatherOutput = true;
		for (String elem : inputs) {
			if (elem.startsWith("-graph"))
				isGraphical = true;
		}
		boolean forceNewProcess = false;
		if (inputs[1].equals("lsco") || inputs[1].equals("lsprivate")) {
			// special cases for very long running subcommands
			forceNewProcess = true;
			// process result from lsprivate async, line by line,
			// by using the OperationListener interface. To speed up processing
			if (listener != null)
				gatherOutput = false;
		}

		if (useSingleprocessLauncher && !forceNewProcess && !isGraphical) {
			launcher = SingleProcessCommandLauncher.getDefault();
		} else {
			launcher = new CommandLauncher();
			if (isGraphical) {
				int graphicalTimeout = TIMEOUT_GRAPHICAL_TOOLS;
				// FIXME: mike 20100624 Check with fb

				try {
					graphicalTimeout = Integer.parseInt(System
							.getProperty("TIMEOUT_GRAPHICAL_TOOLS"));
				} catch (Exception e) {
				}

				((CommandLauncher) launcher).setAutoReturn(graphicalTimeout);
			}
			((CommandLauncher) launcher).setGatherOutput(gatherOutput);
		}

		if (cleartoolCmdline instanceof CleartoolCLICommandLine) {
			CleartoolCLICommandLine cmdline = (CleartoolCLICommandLine) cleartoolCmdline;
			File dir = null;
			if (cmdline.needsWorkingDirectory()) {
				dir = getWorkingDirectory();
			}
			response = launcher.execute(inputs, dir, getCleartoolEnvironment(),
					listener);
		} else {
			response = launcher.execute(inputs, getWorkingDirectory(),
					getCleartoolEnvironment(), listener);
		}

		if (null != listener && listener.isCanceled()) {
			LOG.fine("operation canceled, process terminated"); //$NON-NLS-1$
			operationCanceled();
		}

		LOG.fine("cleartool exit value: " + response.getExitValue()); //$NON-NLS-1$

		// get output
		final String[] output = response.getStdOutMsg();
		final String[] errors = response.getStdErrMsg();
		if (null != errors && errors.length > 0) {
			// merge errors into output for single processing
			String[] mergedOutput = null;
			if (output == null) {
				mergedOutput = errors;
			} else {
				mergedOutput = new String[output.length + errors.length];
				System.arraycopy(output, 0, mergedOutput, 0, output.length);
				System.arraycopy(errors, 0, mergedOutput, output.length,
						errors.length);
			}
			return mergedOutput;
		}

		return output;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#mountVob(java.lang.String)
	 */
	public void mountVob(final String vobName) {
		if (null == vobName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"vobName must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("mount")
				.addElement(vobName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#move(java.lang.String[],
	 * java.lang.String[], java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] move(final String element,
			final String target, final String comment, final int flags,
			final OperationListener operationListener) {
		String[] elements = null;
		ClearCaseElementState[] result = null;
		if (element != null && target != null) {
			elements = new String[] { element, target };
		}
		final List<String> optionsList = new ArrayList<String>();
		// is comment specified
		if (null == comment) {
			optionsList.add("-nc"); //$NON-NLS-1$
		}

		// preserve time
		if (isSet(flags, ClearCase.PTIME)) {
			optionsList.add("-pti");
		}

		final String[] options = optionsList.toArray(new String[optionsList
				.size()]);

		// TODO: Test code.
		// force check out parent element
		if (isSet(flags, ClearCase.FORCE)) {
			final String[] parents = getPath(elements);
			ClearCaseElementState[] states = getElementStates(parents,
					ClearCase.DIR, operationListener);
			// Use states as iterator!!!!
			for (int i = 0; i < states.length; i++) {
				if (states[i].isCheckedIn()) {
					try {
						checkout(new String[] { parents[i] }, null,
								ClearCase.FORCE | (ClearCase.PTIME & flags),
								null);
					} catch (ClearCaseError cce) {
						throw new ClearCaseException();
					}
				}
			}

		}
		try {
			result = ccOperation("mv", options, elements, comment,
					operationListener);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			case ClearCase.ERROR_ALREADY_EXISTS:
				ClearCase.error(ClearCase.ERROR_ALREADY_EXISTS);
				break;
			case ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL:
				ClearCase.error(ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL);
				break;
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}

		// check-in parent directories after move.
		if (isSet(flags, ClearCase.CHECKIN)) {
			for (int i = 0; i < elements.length; i++) {
				final File f = new File(elements[i]);
				final File parent = f.getParentFile();
				if (parent != null) {
					final String parentElement = parent.getAbsolutePath();

					// parent checked-in already?
					final ClearCaseElementState state = getElementState(parentElement);
					if (state.isCheckedIn()) {
						// Do nothing
					} else if (state.isCheckedOut()) {
						checkin(new String[] { parentElement }, null,
								ClearCase.FORCE | (ClearCase.PTIME & flags),
								null);
					}
				}
			}
		}

		return result;
	}

	public ClearCaseElementState[] parserCCOutput(final String[] result) {
		List<ClearCaseElementState> states = new ArrayList<ClearCaseElementState>();
		for (int i = 0; i < result.length; i++) {
			ClearCaseElementState state = matchComposite.check(result[i]);
			if (state != null) {
				states.add(state);
			}
		}
		return states.toArray(new ClearCaseElementState[states.size()]);
	}

	protected String parserOutputCATCS(final String result) {
		LOG.finest("parsing CATCS: " + result); //$NON-NLS-1$
		final String line = null;
		// check error
		if (isError(result))
			return line;
		return result;
	}

	/**
	 * 
	 * @param result
	 * @return empty string if error in result
	 */
	protected String parserOutputLSSTGLOC(final String result) {
		LOG.finest("parsing LSSTGLOC: " + result); //$NON-NLS-1$
		String viewLoc = "";
		// check error
		if (isError(result))
			return viewLoc;

		final int index = result.indexOf('/');
		viewLoc = result.substring(index).trim();
		return viewLoc;
	}

	/**
	 * Parser for the lsview command.
	 * 
	 * @param output
	 *            The output for the lsview command.
	 * @return The type of view.
	 * @throws ClearCaseError
	 *             if the status could not be retreived.
	 */
	protected String parserOutputLSView(String[] output) {
		String viewType = null;
		for (String row : output) {
			if (row.startsWith("Properties: snapshot")) {

				viewType = VIEW_TYPE_SNAPSHOT;
			}
			if (row.startsWith("Properties: dynamic")) {
				viewType = VIEW_TYPE_DYNAMIC;
			}
		}
		if (viewType == null) {
			ClearCase.error(ClearCase.ERROR_INVALID_ARGUMENT,
					"Could not get status information from view");
		}

		return viewType;
	}

	/**
	 * @param result
	 * @return empty string if error in result.
	 */
	protected String parserOutputPWV(final String result) {
		LOG.finest("parsing PWV: " + result); //$NON-NLS-1$
		String viewName = "";
		// check error
		if (isError(result))
			return viewName;
		if (result.equals("** NONE **"))
			return viewName;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#setViewConfigSpec(java.lang
	 * .String, java.lang.String)
	 */
	public void setViewConfigSpec(final String viewName,
			final String configSpecFile, OperationListener listener) {
		this.setViewConfigSpec(viewName, configSpecFile, null, listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#setViewConfigSpec(java.lang
	 * .String, java.lang.String, java.lang.String)
	 */
	public void setViewConfigSpec(String viewName, String configSpecFile,
			String workingDriectory, OperationListener listener) {
		if (null == viewName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"viewname must not be null"); //$NON-NLS-1$
		}
		if (null == configSpecFile) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"configSpecFile must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("setcs")
				.addOption("-force").addOption("-tag").addElement(viewName)
				.addElement(configSpecFile);

		if (workingDriectory != null) {
			setData(WORKING_DIR, new File(workingDriectory));
			((CleartoolCLICommandLine) cleartool)
					.setNeedsWorkingDirectory(true);
		}

		// execute command
		launch(cleartool, listener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#uncheckout(java.lang.String
	 * [], int, net.sourceforge.clearcase.events.OperationListener)
	 */
	public ClearCaseElementState[] uncheckout(final String[] elements,
			final int flags, final OperationListener operationListener) {
		final List<String> optionsList = new ArrayList<String>();
		if (isSet(flags, ClearCase.KEEP)) {
			optionsList.add("-keep"); //$NON-NLS-1$
		} else {
			optionsList.add("-rm"); //$NON-NLS-1$
		}

		final String[] options = Utils.toArray(optionsList);
		return ccOperation("unco", options, elements, null, operationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#unmountVob(java.lang.String)
	 */
	public void unmountVob(final String vobName) {
		if (null == vobName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"vobName must not be null"); //$NON-NLS-1$
		}
		checkInterface();
		final CommandLine cleartool = new CleartoolCLICommandLine("umount")
				.addElement(vobName);
		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

	}

	public void update(String element, int flags, boolean isWorkingDir) {
		if (null == element && isWorkingDir) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"workingDir must not be null"); //$NON-NLS-1$
		} else if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}
		final CleartoolCLICommandLine cleartool = new CleartoolCLICommandLine(
				"update");
		// Always force
		cleartool.addOption("-force");

		if (isSet(flags, ClearCase.GRAPHICAL)) {
			cleartool.addOption("-graphical");
		}

		if (isSet(flags, ClearCase.FORCE)) {
			if (isSet(flags, ClearCase.KEEP)) {
				cleartool.addOption("-ren");
			} else {
				cleartool.addOption("-ove");
			}
		} else {
			cleartool.addOption("-nov");
		}
		// preserve time
		if (isSet(flags, ClearCase.PTIME)) {
			cleartool.addOption("-pti");
		}

		if (isWorkingDir) {
			cleartool.setNeedsWorkingDirectory(true);
			setData(WORKING_DIR, new File(element));
			launch(cleartool, null);
		} else {
			cleartool.addElement(element);
			launch(cleartool, null);
		}
	}

	/*
	 * This merge is supported: About merging the latest version with your
	 * changes. cleartool merge -graphical -to file-or-directory-in-your-view \
	 * file-or-directory-name@@/main/LATEST
	 */
	public void merge(String targetPath, String[] contributorVersions, int flags) {

		final CommandLine cleartool = new CleartoolCLICommandLine("merge")
				.addOptionWithOrder("-graphical").addOptionWithOrder("-to")
				.addElementWithOrder(targetPath);

		// append versions
		for (int i = 0; i < contributorVersions.length; i++) {
			String contributor = contributorVersions[i];
			cleartool.addElementWithOrder(contributor);

		}

		try {
			// execute command
			final String[] result = launch(cleartool, null);
			parserCCOutput(result);
		} catch (ClearCaseException cce) {
			switch (cce.getErrorCode()) {
			default:
				ClearCase.error(ClearCase.ERROR_UNSPECIFIED);
				break;
			}
		}
	}

	/**
	 * Implements a method in ClearCaseInterface.
	 */
	public String getPreviousVersion(String element) {
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}

		final CommandLine cleartool = new CleartoolCLICommandLine("desc")
				.addOption("-s").addOption("-pre").addElement(element);
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		return output[0];
	}

	/**
	 * Implements a method in ClearCaseInterface.
	 */
	public void showVersionTree(String element) {
		showVersionTree(element, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#showVersionTree(java.lang
	 * .String, java.io.File)
	 */
	@Override
	public void showVersionTree(String element, File workingDir) {
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}
		final CommandLine cleartool = new CleartoolCLICommandLine("lsvtree")
				.addOption("-graphical").addElement(element);

		if (workingDir != null) {
			((CleartoolCLICommandLine) cleartool)
					.setNeedsWorkingDirectory(true);
			setData(WORKING_DIR, workingDir);
		}
		launch(cleartool, null);
	}

	/**
	 * Implements a method in ClearCaseInterface.
	 */
	public void showFindMerge(File topdir) {
		if (null == topdir) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"topdir must not be null"); //$NON-NLS-1$
		}
		final CleartoolCLICommandLine cleartool = new CleartoolCLICommandLine(
				"findmerge");
		cleartool.addOption("-graphical");
		cleartool.setNeedsWorkingDirectory(true);
		setData(WORKING_DIR, topdir);
		launch(cleartool, null);
	}

	/**
	 * Implements a method in ClearCaseInterface.
	 */
	public void compareWithPredecessor(String element) {
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}
		final CommandLine cleartool = new CleartoolCLICommandLine("diff")
				.addOption("-graphical").addOption("-pred").addElement(element);
		// mike: No need to check result for graphical tools
		launch(cleartool, null);
	}

	/**
	 * Implements a method in ClearCaseInterface.
	 */
	public void describeVersionGUI(String element) {
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}
		final CommandLine cleartool = new CleartoolCLICommandLine("describe")
				.addOption("-graphical").addElement(element);
		launch(cleartool, null);
	}

	/**
	 * Implements method in ClearCaseInterface.
	 */
	public void compareWithVersion(String element1, String element2) {
		if (null == element1 || null == element2) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"no elements must  be null"); //$NON-NLS-1$
		}
		final CommandLine cleartool = new CleartoolCLICommandLine("diff")
				.addOption("-graphical").addElement(element1).addElement(
						element2);
		// mike: No need to check the result for graphical tools
		launch(cleartool, null);

	}

	private String[] getPath(String[] elements) {
		List<String> files = new ArrayList<String>();
		for (int i = 0; i < elements.length; i++) {
			File f = new File(elements[i]);
			File parent = f.getParentFile();
			if (parent != null) {
				files.add(parent.getAbsolutePath());
			}
		}
		return files.toArray(new String[files.size()]);
	}

	/**
	 * Returns the history string of the elemtent.
	 * 
	 * @param viewName
	 *            the wiew name
	 * @param element
	 *            the element
	 * @return History String
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the element is null</li>
	 *                </ul>
	 * @exception ClearCaseException
	 *                <ul>
	 *                <li>ERROR_INTERFACE_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_NOT_ACCESSIBLE - if an element is not accessible
	 *                </li>
	 *                </ul>
	 * 
	 */
	public Vector<ElementHistory> getElementHistory(String element) {
		int counter = -1;
		String user = "";
		String date = "";
		String version = null;
		String label = "";
		String comment = "";
		Vector<ElementHistory> history = new Vector<ElementHistory>();

		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}
		checkInterface();

		CommandLine cleartool;

		if (element.contains("@@")) {
			cleartool = new CleartoolCLICommandLine("lshistory").addOption(
					"-long").addOption("-directory").addElement(element);
		} else {
			File f = new File(element);

			if (!f.exists()) {
				ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
						"element must be an existing file or directory"); //$NON-NLS-1$
			}

			if (f.isDirectory()) {
				cleartool = new CleartoolCLICommandLine("lshistory").addOption(
						"-long").addOption("-directory").addElement(
						element + "@@");
			} else {
				cleartool = new CleartoolCLICommandLine("lshistory").addOption(
						"-long").addElement(element + "@@");
			}
		}

		// execute command
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		// process command output

		Pattern dateUserPattern = Pattern
				.compile("^(\\d{4}-\\d{2}-\\d{2}T[^ ]*|\\d{2}-\\w{3}-\\d{2}\\.\\d{2}:\\d{2}:\\d{2})\\s+([^(.]*).*$");
		Pattern versionLabelPattern = Pattern
				.compile("^  create version[^@]+@@([^\"]+)[^(]+([(]([^)]+)[)])?.*$");
		Pattern versionDirPattern = Pattern
				.compile("^  create directory version[^@]+@@([^\"]+)[^(]+([(]([^)]+)[)])?.*$");
		Matcher m;
		for (int i = 0; i < output.length; i++) {
			if ((m = dateUserPattern.matcher(output[i])).matches()) {

				if (version != null) {
					history.add(new ElementHistory(element, date, user,
							version, label, comment));
				}

				counter = 0;
				user = m.group(2);
				date = m.group(1);
				version = null;
				comment = "";
			} else {
				counter++;
				if (((m = versionLabelPattern.matcher(output[i])).matches())
						&& (counter == 1)) {
					version = m.group(1);
					label = m.group(3);
				} else if (((m = versionDirPattern.matcher(output[i]))
						.matches())
						&& (counter == 1)) {
					version = m.group(1);
					label = m.group(3);
				} else {
					comment = comment + " " + output[i];
				}
			}
		}
		if (version != null) {
			history.add(new ElementHistory(element, date, user, version, label,
					comment));
		}

		return history;
	}

	/**
	 * Search Files that has been modified into a branch.
	 * 
	 * @param branchName
	 *            Name of the branch
	 * @param workingDir
	 *            Clearcase working directory
	 * @return Files into the branch
	 */
	public String[] searchFilesInBranch(String branchName, File workingDir,
			OperationListener listener) {
		String[] output = null;

		if (null == branchName) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"branchName must not be null"); //$NON-NLS-1$
		}
		if (null == workingDir) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"workingDir must not be null"); //$NON-NLS-1$
		}

		final CommandLine cleartool = new CleartoolCLICommandLine("find")
				.addOption("-all").addOption("-print").addOption("-branch")
				.addElement("\'brtype(" + branchName + ")\'");

		((CleartoolCLICommandLine) cleartool).setNeedsWorkingDirectory(true);
		setData(WORKING_DIR, workingDir);

		output = launch(cleartool, listener);

		return output;
	}

	/**
	 * Get list of branches available into a specific clearcase path.
	 * 
	 * @param workingDir
	 *            the current working directory
	 * @return List of branches.
	 */
	public String[] loadBrancheList(File workingDir) {
		String[] output = null;
		ArrayList<String> result = new ArrayList<String>();

		if (null == workingDir) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"workingDir must not be null"); //$NON-NLS-1$
		}

		final CommandLine cleartool = new CleartoolCLICommandLine("lstype")
				.addOption("-kind").addElement("brtype");

		((CleartoolCLICommandLine) cleartool).setNeedsWorkingDirectory(true);
		setData(WORKING_DIR, workingDir);

		output = launch(cleartool, null);

		Pattern branchPatern = Pattern
				.compile("^[-:A-Z0-6]+[^\"]+[\"](.*)[\"][^\"]*$");
		Matcher m;
		for (String line : output) {
			if ((m = branchPatern.matcher(line)).matches()) {
				if ((m.group(1) == null) || m.group(1).equals("")) {
					System.err.println("error " + line);
				} else {
					result.add(m.group(1));
				}
			}
		}
		output = new String[result.size()];

		return result.toArray(output);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * see net.sourceforge.clearcase.ClearCaseInterface#checkedOutType(String)
	 */
	@Override
	public String checkedOutType(String element) {
		if (null == element) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}

		final CommandLine cleartool = new CleartoolCLICommandLine("desc")
				.addOption("-fmt").addOption("\"\\\"\\tStatus: %Rf\\n\"")
				.addElement(element);
		final String[] output = launch(cleartool, null);
		// check for result
		if (output.length == 0) {
			errorEmptyResult();
		}

		return output[0];

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.ClearCaseInterface#setDebugLevel(int)
	 */
	@Override
	public void setDebugLevel(int level) {
		debugLevel = level;
	}

	public static int getDebugLevel() {
		return debugLevel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#reserved(java.lang.String[],
	 * java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	@Override
	public ClearCaseElementState[] reserved(final String[] elements,
			final String comment, final int flags,
			final OperationListener operationListener) {

		if (0 == elements.length) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}

		final List<String> optionsList = new ArrayList<String>();

		if (null == comment || "".equals(comment.trim())) {
			optionsList.add("-nc"); //$NON-NLS-1$
		} else {
			optionsList.add("-c");
			optionsList.add(Utils.escapeComment(comment));
		}

		final String[] options = Utils.toArray(optionsList);

		return ccOperation("reserve", options, elements, null,
				operationListener);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.ClearCaseInterface#unreserved(java.lang.String
	 * [], java.lang.String, int,
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	@Override
	public ClearCaseElementState[] unreserved(final String[] elements,
			final String comment, final int flags,
			final OperationListener operationListener) {

		if (0 == elements.length) {
			ClearCase.error(ClearCase.ERROR_NULL_ARGUMENT,
					"element must not be null"); //$NON-NLS-1$
		}

		final List<String> optionsList = new ArrayList<String>();

		if (null == comment || "".equals(comment.trim())) {
			optionsList.add("-nc"); //$NON-NLS-1$
		} else {
			optionsList.add("-c");
			optionsList.add(Utils.escapeComment(comment));
		}

		final String[] options = Utils.toArray(optionsList);

		return ccOperation("unreserve", options, elements, null,
				operationListener);

	}

}
