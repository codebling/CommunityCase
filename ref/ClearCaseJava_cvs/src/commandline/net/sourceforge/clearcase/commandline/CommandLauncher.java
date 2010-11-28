/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gunnar Wagenknecht - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.clearcase.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseCLIImpl;
import net.sourceforge.clearcase.events.OperationListener;

/**
 * This class executes a CLI command.
 */
public class CommandLauncher implements ICommandLauncher {

	// Used for testing
	private static boolean test = false;
	private boolean gatherOutput = true;

	/**
	 * Returns the gatherOutput.
	 * 
	 * @return returns the gatherOutput
	 */
	public boolean isGatherOutput() {
		return gatherOutput;
	}

	/**
	 * Sets the value of gatherOutput.
	 * 
	 * @param gatherOutput
	 *            the gatherOutput to set
	 */
	public void setGatherOutput(boolean gatherOutput) {
		this.gatherOutput = gatherOutput;
	}

	private static String[] msg = null;

	private StreamReaderThread out;
	private StreamReaderThread err;
	private ExecutionManagementThread execMgr = null;

	private class ExecutionManagementThread extends Thread {
		private boolean finished = false;
		private OperationListener opListener = null;
		private Process process = null;

		/**
		 * Creates a new instance.
		 * 
		 * @param opListener
		 * @param process
		 */
		public ExecutionManagementThread(OperationListener opListener,
				Process process) {
			super();
			this.opListener = opListener;
			this.process = process;
		}

		@Override
		public synchronized void run() {
			try {
				while (!finished && opListener != null) {
					sleep(100);
					if (opListener.isCanceled()) {
						process.destroy();
						finished = true;
					}
				}
			} catch (InterruptedException ex) {
			}
		}

		/**
		 * Sets the value of finished.
		 * 
		 * @param finished
		 *            the finished to set
		 */
		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}

	/* Inner stream reader class */
	private class StreamReaderThread extends Thread {
		private BufferedReader br = null;
		private List<String> lines = null;
		// private String newline = null;
		private String line = null;
		private OperationListener opListener = null;
		private boolean isError = false;

		public StreamReaderThread(OperationListener opListener, boolean isError) {
			this.opListener = opListener;
			this.isError = isError;
			if (isError) {
				this.setName("CC stderr reader 2");
			} else {
				this.setName("CC stdout reader 2");
			}
		}

		public synchronized String[] getOutput() {
			if (lines == null)
				return null;
			return lines.toArray(new String[lines.size()]);

		}

		// for testing set output.
		private synchronized void setOutput(String[] output) {
			for (int i = 0; i < output.length; i++) {
				if (lines == null) {
					lines = new ArrayList<String>();
				}
				lines.add(output[i]);
			}
		}

		public synchronized void setInputStream(InputStream is) {
			br = new BufferedReader(new InputStreamReader(is));
			notify();
		}

		@Override
		public synchronized void run() {
			try {
				if (br == null) {
					// waits only if the input stream has not been initialized
					wait();
				}
				while ((line = br.readLine()) != null) {
					if (ClearCaseCLIImpl.getDebugLevel() > 0) {
						if (isError) {
							System.out.println("<<# " + line);
						} else {
							System.out.println("<<< " + line);
						}
					}
					if (opListener != null) {
						if (isError) {
							opListener.printErr(line);
						} else {
							opListener.print(line);
						}
					}
					if (gatherOutput) {
						if (lines == null) {
							lines = new ArrayList<String>();

						}
						lines.add(line);
					}
				}
			} catch (IOException ex) {
			} catch (InterruptedException ex) {
			}
		}
	}

	/** the exit value (defaults to -1) */
	private int exitValue = -1;

	private int autoReturn = 0;

	/**
	 * Sets the value of autoReturn. After this many seconds the launcher
	 * returns automatically with exit value 0 if the process is still running.
	 * Useful for launching graphical tools.
	 * 
	 * @param autoReturn
	 *            the autoReturn to set
	 */
	public void setAutoReturn(int autoReturn) {
		this.autoReturn = autoReturn;
	}

	/**
	 * Creates a new instance and executes the specified command.
	 * 
	 * @param command
	 *            the command line
	 * @param workingDir
	 *            the working directory of the subprocess, or <tt>null</tt> if
	 *            the subprocess should inherit the working directory of the
	 *            current process.
	 * @param env
	 *            array of strings, each element of which has environment
	 *            variable settings in format <i>name </i>= <i>value </i>.If
	 *            <code>null</code>, the environment of the current process is
	 *            used.
	 * @param listener
	 *            an operation listener (maybe <code>null</code>) that will be
	 *            asked if the running process should be canceled
	 */

	public CommandLauncher() {

	}

	/*
	 * Make sure it is thread safe.
	 */
	public Response execute(String[] command, File workingDir, String[] env,
			OperationListener listener) {
		if (null == command)
			throw new IllegalArgumentException("Command must not be null"); //$NON-NLS-1$

		Response response = new Response();
		Process process = null;
		long start = System.currentTimeMillis();

		if (test) {
			// Feed output from cleartool cmd to String [] out and err.
			out = new StreamReaderThread(listener, false);
			err = new StreamReaderThread(listener, true);
			out.setDaemon(true);
			err.setDaemon(true);
			execMgr = new ExecutionManagementThread(listener, process);
			out.setOutput(msg);

		} else {
			out = new StreamReaderThread(listener, false);
			err = new StreamReaderThread(listener, true);

			try {
				if (ClearCaseCLIImpl.getDebugLevel() > 0) {
					System.out.print(">   exec() in " + workingDir + "\n>>> ");
					for (int i = 0; i < command.length; i++) {
						System.out.print(" " + command[i]);
					}
					if (!gatherOutput) {
						System.out
								.println("\n>>>  output handling via callback");
					} else {
						System.out.println("");
					}
				}
				// Start threads storing the output of the basecommand.
				out.start();
				err.start();
				if (listener != null) {
					String cmd = "";
					for (String s : command) {
						cmd += s + " ";
					}

					listener.printInfo("Command:" + cmd);
					if (workingDir != null) {
						listener.printInfo("Working Directory:" + workingDir);
					}
					listener.printInfo("");
				}

				process = Runtime.getRuntime().exec(command, env, workingDir);

				execMgr = new ExecutionManagementThread(listener, process);
				execMgr.start();
				// Set input streams and wakeup reader threads
				out.setInputStream(process.getInputStream());
				err.setInputStream(process.getErrorStream());

				if (autoReturn > 0) {
					// automatically return after the given number of seconds,
					// or if the process dies
					for (int i = autoReturn * 5; i > 0; i--) {
						Thread.sleep(200);
						if (!out.isAlive()) {
							// process exited before timeout is over.
							// flag that the process exited and we can call
							// process.waitFor() later
							autoReturn = 0;
							break;
						}
					}
					// force streams to close
					// process.getInputStream().close();
					// process.getErrorStream().close();
				} else {

					synchronized (out) {
						if (out.isAlive()) {
							try {
								out.wait();
							} catch (InterruptedException ie) {
							}
						}
					}

					synchronized (err) {
						if (err.isAlive()) {
							try {
								err.wait();
							} catch (InterruptedException ie) {
							}
						}
					}
				}

				if (autoReturn > 0)
					exitValue = 0; // do not wait for process
				else
					exitValue = process.waitFor();

				if (ClearCaseCLIImpl.getDebugLevel() > 0) {
					long currentTimeMillis = System.currentTimeMillis();
					long elapsedTimeMillis = currentTimeMillis - start;
					System.out.println("<<  duration: " + elapsedTimeMillis
							+ " ms  s=" + start + "  e=" + currentTimeMillis);
				}
			} catch (IOException e) {
				ClearCase.error(ClearCase.ERROR_IO, e, null);
			} catch (InterruptedException ie) {
				ClearCase.error(ClearCase.ERROR_IO, ie, null);

			} finally {
				execMgr.setFinished(true);
				// if (null != canceler) canceler.stop();
				try {
					if (autoReturn == 0)
						closeStreams(process); // would block
					process.destroy();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

		}
		if (autoReturn == 0) {
			// would block if streams still open
			response.setStdOutMsg(getOutput());
			response.setStdErrMsg(getErrorOutput());
		}
		response.setExitValue(exitValue);
		return response;
	}

	/**
	 * Close file handles created by process.
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6462165
	 * 
	 * @param p
	 * @throws IOException
	 */
	static void closeStreams(Process p) throws IOException {
		p.getOutputStream().close();
		p.getErrorStream().close();
	}

	/**
	 * Returns the error output.
	 * 
	 * @return the error output
	 */
	public String[] getErrorOutput() {
		return this.err.getOutput();
	}

	/**
	 * Returns the exit value.
	 * 
	 * @return the exit value
	 */
	public int getExitValue() {
		return exitValue;
	}

	/**
	 * Returns the process output.
	 * 
	 * @return the process output
	 */
	public String[] getOutput() {
		return this.out.getOutput();
	}

	/*
	 * Enable testing ( no clearcase installed).
	 */
	public static void forTest() {
		test = true;

	}

	/*
	 * Set test response messages for standard out and error.
	 */
	public static void setResponse(Response rsp) {
		msg = rsp.getStdOutMsg();

	}
}