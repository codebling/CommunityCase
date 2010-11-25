/*******************************************************************************
 * Copyright (c) 2002, 2007 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/

package net.sourceforge.clearcase.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.clearcase.events.OperationListener;

/**
 * TODO Provide description for Command.
 */
public class Command {

	private static final String CHECKOUT = "checkout";

	private static final String CHECKIN = "checkin";

	private static final String DELETE = "delete";

	private static final String CREATE_DIR = "create_dir";

	private static final String CREATE_FILE = "create_file";

	private static final String CONFIG_SPEC = "configspec";

	private static final String VOB_NAMES = "vobnames";

	private static final String VIEW_NAMES = "viewnames";

	private static final String LS = "list";

	private static final String INTERACT = "interact";

	private static void interact(ClearCaseInterface cci) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String c = null;
		while (true) {
			try {
				c = br.readLine();
			} catch (IOException e) {
				// TODO handle catch block
				System.exit(1);
			}
			if (c.startsWith("ls ")) {
				String[] opts = new String[1];
				opts[0] = c.substring(3);
				@SuppressWarnings("unused")
				ClearCaseElementState state = cci.getElementState(opts[0]);
				// ClearCaseElementState[] states = cci.getElementStates(opts,
				// 0, null);
			} else if (c.startsWith("x")) {
				System.exit(0);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Number of args :" + args.length);
		if (args.length < 1) {
			printUsage();
		}
		ClearCaseInterface cci = ClearCase
				.createInterface(ClearCase.INTERFACE_CLI);
		// Size is options minus first command.
		String cmd = args[0];
		String[] opts = null;
		if (args[1].equals("*")) {
			opts = elementList();
			if (opts == null) {
				System.err.println("No elements found in directory");
				System.exit(1);
			}
		} else {
			// One or more specified elements
			opts = new String[args.length - 1];
			int j = 0;
			for (int i = 1; i < args.length; i++) {
				opts[j] = args[i];
				j++;
			}
		}

		printArgs(cmd, opts);

		if (cmd.equals(CHECKOUT) && args[1] != null) {

			OperationListener op = new OperationListenerImpl();
			cci.checkout(opts, null, 0, op);
		} else if (cmd.equals(CHECKIN) && args[1] != null) {

			OperationListener op = new OperationListenerImpl();
			cci.checkin(opts, null, 0, op);
		} else if (cmd.equals(DELETE) && args[1] != null) {

			OperationListener op = new OperationListenerImpl();
			cci.delete(opts, null, 0, op);
		} else if (cmd.equals(CREATE_DIR) && args[1] != null) {

			OperationListener op = new OperationListenerImpl();
			for (int i = 0; i < opts.length; i++) {
				String element = opts[i];

				cci.add(element, true, null, 0, op);
			}

		} else if (cmd.equals(CREATE_FILE) && args[1] != null) {
			OperationListener op = new OperationListenerImpl();
			for (int i = 0; i < opts.length; i++) {
				String element = opts[i];

				cci.add(element, false, null, 0, op);
			}
		} else if (cmd.equals(CONFIG_SPEC) && args[1] != null) {
			String output = cci.getViewConfigSpec(opts.toString());
			System.out.println(output);
		} else if (cmd.equals(VOB_NAMES)) {
			OperationListener op = new OperationListenerImpl();
			String[] output = cci.getVobNames(op);
			outputRes(output);
		} else if (cmd.equals(VIEW_NAMES)) {
			// OperationListener op = new OperationListenerImpl();
			String[] output = cci.getViewNames();
			outputRes(output);
			cci.getViewConfigSpec(opts.toString());
		} else if (cmd.equals(LS) && args[1] != null) {
			OperationListener op = new OperationListenerImpl();
			ClearCaseElementState[] states = cci.getElementStates(opts, 0, op);
			cci.getElementStates(opts, 0, null);
			cci.getVobNames(null);
			for (int i = 0; i < states.length; i++) {
				ClearCaseElementState clearCaseElementState = states[i];

				String elementState = clearCaseElementState.toString();
				System.out.println(elementState);
			}
		} else if (cmd.equals(INTERACT)) {
			interact(cci);
		} else {
			System.err.println("Console does not support command ");
			System.exit(1);
		}
		System.exit(0);

	}

	private static void printUsage() {
		System.err
				.println("Usage: java -jar clearcase-java.jar command [option]");
		System.err
				.println("Command - checkout,checkin,delete,create_dir, create_file,configspec,vobnames,viewnames,list");
		System.out.println("Option - *, element, list of elements, none");
		System.exit(1);
	}

	private static void printArgs(String command, String[] options) {
		System.out.println("Command : " + command);
		System.out.println("Options :");
		for (int i = 0; i < options.length; i++) {
			String opt = options[i];
			System.out.println(opt);

		}
	}

	private static String[] elementList() {
		// Get current directory where the java -jar command was executed.
		File currentDir = new File("."); // TODO: Or use
		// System.getProperty("user.dir") if
		// (files == null)
		return currentDir.list();

	}

	private static void outputRes(String[] output) {
		for (int i = 0; i < output.length; i++) {
			String line = output[i];
			System.out.println(line + "\n");

		}
	}
}
