/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.eclipseccase;

import net.sourceforge.clearcase.commandline.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;

/**
 * This is a helper class for accesing the "ClearDlg" executable.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
public class ClearDlgHelper {

	private static final String _UNCHECKOUT = "/uncheckout"; //$NON-NLS-1$

	private static final String _CHECKOUT = "/checkout"; //$NON-NLS-1$

	private static final String _CHECKIN = "/checkin"; //$NON-NLS-1$

	private static final String _ADDTOSRC = "/addtosrc"; //$NON-NLS-1$

	private static final String _DIFFPRED = "/diffpred"; //$NON-NLS-1$
	
	/**
	 * Adds the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void add(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(_ADDTOSRC, resources);
	}

	/**
	 * Checkin the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void checkin(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(_CHECKIN, resources);
	}

	/**
	 * Checkout the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void checkout(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(_CHECKOUT, resources);
	}

	/**
	 * Uncheckout the specified resources.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void uncheckout(IResource[] resources) throws TeamException {
		// execute cleardlg
		perform(_UNCHECKOUT, resources);
	}

	/**
	 * Diff to predecessor for specified resource.
	 * 
	 * @param resources
	 * @throws TeamException
	 */
	public static void diffpred(IResource resource) throws TeamException {
		// execute cleardlg
		perform(_DIFFPRED, new IResource[] {resource});
	}
	
	/**
	 * Adds the specified resources.
	 * 
	 * @param operation
	 * @param resources
	 * @throws TeamException
	 */
	public static void perform(String operation, IResource[] resources) throws TeamException {
		// prepare command
		CommandLine cleardlg = new CleardlgCommandLine(operation);

		// append files
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			cleardlg.addElement(resource.getLocation().toOSString());
		}

		// execute cleardlg
		CommandLauncher launcher = new CommandLauncher();
		launcher.execute(cleardlg.create(), null, null, null);

		int returnValue = launcher.getExitValue();
		if (0 != returnValue && returnValue != resources.length)
			throw new TeamException("Execution of cleardlg " + operation + " failed (process returned: " + returnValue + ")!");
		// throw new TeamException("Execution of cleardlg " + operation
		// + " failed: " + e.getMessage(), e);

		// refresh resources
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);

			// refresh resource
			provider.refresh(resource);

			// also invalidate state of parent container
			// (some operations my checkout parent)
			provider.refresh(resource.getParent());
		}
	}

	/**
	 * No instance necessary.
	 */
	private ClearDlgHelper() {
		super();
	}

}