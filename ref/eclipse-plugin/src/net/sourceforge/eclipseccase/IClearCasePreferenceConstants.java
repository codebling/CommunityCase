/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas taken from Eclipse code
 *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
 *******************************************************************************/
package net.sourceforge.eclipseccase;

/**
 * Shared preference constants for ClearCase plugin preferences.
 */
public interface IClearCasePreferenceConstants {
	/** ClearCase preference */
	String ADD_AUTO = ClearCasePlugin.PLUGIN_ID + ".add.auto"; //$NON-NLS-1$

	/** ClearCase preference */
	String CHECKIN_IDENTICAL = ClearCasePlugin.PLUGIN_ID + ".checkin.identical"; //$NON-NLS-1$

	/** ClearCase preference */
	String ADD_WITH_CHECKIN = ClearCasePlugin.PLUGIN_ID + ".add.checkin"; //$NON-NLS-1$

	/** ClearCase preference */
	String ADD_WITH_MASTER = ClearCasePlugin.PLUGIN_ID + ".add.master"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String CHECKOUT_AUTO = ClearCasePlugin.PLUGIN_ID + ".checkout.auto"; //$NON-NLS-1$

	/** ClearCase preference */
	String CHECKOUT_LATEST = ClearCasePlugin.PLUGIN_ID + ".checkout.latest"; //$NON-NLS-1$

	/** ClearCase preference */
	String FULL_REFRESH = ClearCasePlugin.PLUGIN_ID + ".refresh.full"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String CHECKOUT_RESERVED = ClearCasePlugin.PLUGIN_ID + ".checkout.reserved"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_ADD = ClearCasePlugin.PLUGIN_ID + ".comment.add"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_ADD_NEVER_ON_AUTO = ClearCasePlugin.PLUGIN_ID
			+ ".comment.add.neverOnAuto"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_CHECKIN = ClearCasePlugin.PLUGIN_ID + ".comment.checkin"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_CHECKOUT = ClearCasePlugin.PLUGIN_ID + ".comment.checkout"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_CHECKOUT_NEVER_ON_AUTO = ClearCasePlugin.PLUGIN_ID
			+ ".comment.checkout.neverOnAuto"; //$NON-NLS-1$

	/** ClearCase preference */
	String COMMENT_ESCAPE = ClearCasePlugin.PLUGIN_ID + ".comment.escape"; //$NON-NLS-1$

	/** ClearCase preference */
	String IGNORE_NEW = ClearCasePlugin.PLUGIN_ID + ".ignore.new"; //$NON-NLS-1$

	/** ClearCase preference */
	String PRESERVE_TIMES = ClearCasePlugin.PLUGIN_ID + ".preserveTimes"; //$NON-NLS-1$

	/** ClearCase preference */
	String RECURSIVE = ClearCasePlugin.PLUGIN_ID + ".recursive"; //$NON-NLS-1$

	/** common preference */
	String SAVE_DIRTY_EDITORS = ClearCasePlugin.PLUGIN_ID + ".saveDirtyEditors"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String USE_SINGLE_PROCESS = ClearCasePlugin.PLUGIN_ID + ".useSingleProcess"; //$NON-NLS-1$

	/** ClearCase preference */
	String PREVENT_UNNEEDED_CHILDREN_REFRESH = ClearCasePlugin.PLUGIN_ID
			+ ".preventRefreshChildren"; //$NON-NLS-1$

	/** ClearCase preference */
	String CLEARCASE_PRIMARY_GROUP = ClearCasePlugin.PLUGIN_ID
			+ ".clearcasePrimaryGroup"; //$NON-NLS-1$
	
	String BRANCH_PREFIX = ClearCasePlugin.PLUGIN_ID
	+ ".branch.prefix"; //$NON-NLS-1$

	/** ClearCase preference */
	String USE_CLEARDLG = ClearCasePlugin.PLUGIN_ID + ".useClearDlg"; //$NON-NLS-1$

	/** ClearCase preference */
	String CLEARCASE_API = ClearCasePlugin.PLUGIN_ID + ".clearcaseAPI"; //$NON-NLS-1$

	/** ClearCase preference */
	String TEST_LINKED_PARENT_IN_CLEARCASE = ClearCasePlugin.PLUGIN_ID
			+ ".testLinkedParentInClearCase"; // //$NON-NLS-1$

	/** ClearCase preference */
	String JOB_QUEUE_PRIORITY = ClearCasePlugin.PLUGIN_ID + ".jobQueuePriority"; // //$NON-NLS-1$

	/** ClearCase preference */
	String KEEP_CHANGES_AFTER_UNCHECKOUT = ClearCasePlugin.PLUGIN_ID
			+ ".keepAfterUncheckout"; // //$NON-NLS-1$;

	/** preference value for <code>CLEARCASE_API</code> */
	String CLEARCASE_NATIVE = "native_cal"; //$NON-NLS-1$

	/** preference value for <code>CLEARCASE_API</code> */
	String CLEARCASE_CLEARTOOL = "native_cleartool"; //$NON-NLS-1$

	/** preference value for <code>CLEARCASE_API</code> */
	String CLEARCASE_CLEARDLG = "compatible_cleardlg"; //$NON-NLS-1$

	/** preference value */
	String IF_POSSIBLE = "ifPossible"; //$NON-NLS-1$

	/** preference value */
	String ALWAYS = "always"; //$NON-NLS-1$

	/** preference value */
	String NEVER = "never"; //$NON-NLS-1$

	/** preference value */
	String PROMPT = "prompt"; //$NON-NLS-1$

	/** ClearCase preference */
	String HIDE_REFRESH_STATE_ACTIVITY = ClearCasePlugin.PLUGIN_ID
			+ ".refreshState.hide"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String TIMEOUT_GRAPHICAL_TOOLS = ClearCasePlugin.PLUGIN_ID
			+ ".graphical.timeout"; //$NON-NLS-1$

	/** ClearCase preference */
	String GRAPHICAL_EXTERNAL_UPDATE_VIEW = ClearCasePlugin.PLUGIN_ID
	+ ".graphical.external.update.view"; //$NON-NLS-1$
	
	String FORBID_CONFIG_SPEC_MODIFICATION = ClearCasePlugin.PLUGIN_ID
	+ ".config_spec.forbid_modification"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String AUTO_PARENT_CHECKIN_AFTER_MOVE = ClearCasePlugin.PLUGIN_ID
			+ ".auto.parent.checkin.after.move"; //$NON-NLS-1$
	
	/** ClearCase preference */
	String COMPARE_EXTERNAL = ClearCasePlugin.PLUGIN_ID + ".compare.external"; //$NON-NLS-1$
	

}
