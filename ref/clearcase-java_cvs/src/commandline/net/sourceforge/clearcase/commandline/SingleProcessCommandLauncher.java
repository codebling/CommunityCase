/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Achim Bursian - basic implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *******************************************************************************/
package net.sourceforge.clearcase.commandline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseCLIImpl;
import net.sourceforge.clearcase.events.OperationListener;

/**
 * This class launches a persistent cleartool process in commandline mode, later
 * it executes single CLI commands by sending them to this process via stdin and
 * parsing the output that is received on stdout and stderr of the subprocess.
 * This is MUCH faster than launching a new cleartool subprocess for each
 * command.
 * 
 * @author Achim Bursian
 */
public class SingleProcessCommandLauncher implements ICommandLauncher {
	private static final SingleProcessCommandLauncher singleton = new SingleProcessCommandLauncher();
	/**
	 * field <code>specialCharPat</code> If a comment contains one of these
	 * chars, a special method -cfile is used to pass the comment to cleartool
	 */
	Pattern specialCharPat = Pattern.compile("[\\t\\r\\n\"\\\\]");

	/**
	 * Accessor for the singleton instance
	 * 
	 * @return The SingleProcessCommandLauncher instance
	 */
	public static SingleProcessCommandLauncher getDefault() {
		return singleton;
	}

	private StreamReaderThread stdoutReader;
	private StreamReaderThread stderrReader;
	private ExecutionManagementThread execMgr = null;
	private boolean commandFinished = false;
	private Object execGuard = new Object();

	/**
	 * Returns the commandFinished.
	 * 
	 * @return returns the commandFinished
	 */
	public synchronized boolean isCommandFinished() {
		return commandFinished;
	}

	/**
	 * Sets the value of commandFinished.
	 * 
	 * @param commandFinished
	 *            the commandFinished to set
	 */
	public synchronized void setCommandFinished(boolean commandFinished) {
		this.commandFinished = commandFinished;
		if (commandFinished) {
			notifyAll();
		}
	}

	/**
	 * This class is responsible for shutting down the subprocess if the
	 * attached OperationListener signals it. opListener.isCanceled() is polled
	 * for this.
	 */
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
			super("CC Execution Management");
			this.opListener = opListener;
			this.process = process;
		}

		@Override
		public void run() {
			while (!finished) {
				try {
					sleep(250);
				} catch (InterruptedException ex) {
				}
				synchronized (this) {
					if (opListener != null && opListener.isCanceled()) {
						process.destroy();
						finished = true;
					}
				}
			}
		}

		public synchronized void setOperationListener(OperationListener ol) {
			opListener = ol;
		}

	}

	/* Inner stream reader class */
	private class StreamReaderThread extends Thread {
		private BufferedReader br = null;
		private List<String> lines = new Vector<String>();
		// private String newline = null;
		private String line = null;
		private OperationListener opListener = null;

		/**
		 * Sets the value of opListener.
		 * 
		 * @param opListener
		 *            the opListener to set
		 */
		public synchronized void setOperationListener(
				OperationListener opListener) {
			this.opListener = opListener;
		}

		private boolean isError = false;
		SingleProcessCommandLauncher parent;

		public StreamReaderThread(OperationListener opListener,
				boolean isError, SingleProcessCommandLauncher parent) {
			this.opListener = opListener;
			this.isError = isError;
			this.parent = parent;
			if (isError) {
				this.setName("CC stderr reader");
			} else {
				this.setName("CC stdout reader");
			}
		}

		public String[] getOutput() {
			synchronized (lines) {
				String[] res = lines.toArray(new String[lines.size()]);
				return res;
			}
		}

		public void reset() {
			synchronized (lines) {
				lines.clear();
			}
		}

		public synchronized void setInputStream(InputStream is) {
			br = new BufferedReader(new InputStreamReader(is));
			notify();
		}

		@Override
		public void run() {
			try {
				if (br == null) {
					// waits only if the input stream has not been initialized
					synchronized (this) {
						wait();
					}
				}
				String tempLine;

				while ((line = br.readLine()) != null) {
					tempLine = line.trim();

					// sometimes the carriage return gets lost, which renders
					// the "Command xx returned status yy"
					// somewhere at the end of the line, not at line start. We
					// have to catch these cases, too, to make it more stable
					// (otherwise it might hang)
					if (!isError
							&& tempLine.contains("Command ")
							&& tempLine.contains(" returned status ")
							&& tempLine.indexOf("Command ") < tempLine
									.indexOf(" returned status ")) {
						int offs = line.indexOf("returned");
						int status = -1;
						status = Integer.parseInt(line.substring(offs + 16));
						if (ClearCaseCLIImpl.getDebugLevel() > 0) {
							System.out.println("<<<<<< " + line);
							if (line.indexOf("Command ") != 0) {
								// special handling if not at line start
								System.out
										.println("!!! missing CR on previous line");
							}
						}
						parent.setExitValue(status);
						parent.setCommandFinished(true);
					} else {
						synchronized (lines) {
							lines.add(line);
						}
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
					}
				}
			} catch (IOException ex) {
			} catch (InterruptedException ex) {
			}
			// tell the main thread that we die
			parent.setExitValue(-1);
			parent.setCommandFinished(true);
		}
	}

	/** the exit value (defaults to -1) */
	private int exitValue = -1;

	private Process process;

	public SingleProcessCommandLauncher() {
		process = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.ICommandLauncher#execute(java.lang
	 * .String[], java.io.File, java.lang.String[],
	 * net.sourceforge.clearcase.events.OperationListener)
	 */
	public Response execute(String[] command, File workingDir, String[] env,
			OperationListener listener) {
		if (null == command)
			throw new IllegalArgumentException("Command must not be null"); //$NON-NLS-1$
		Response response = new Response();
		File temp = null;

		// needs an extra sync block, because the general lock on "this" would
		// be released during the wait() call. But we must be sure that only one
		// thread at a time can process a cleartool command
		synchronized (execGuard) {
			// TODO watchdog for long running cmds (Achim Feb 5, 2010)
			long start = System.currentTimeMillis();
			try {
				String cmd = "";
				int commentIndex = -1;
				// ignore first element! ("cleartool")
				for (int i = 1; i < command.length; i++) {
					Matcher m = specialCharPat.matcher(command[i]);
					if (commentIndex == i && m.find()) {
						// this is the comment string, with special chars
						// inside. So we use the -cfile method to pass them to
						// cleartool.
						// Maybe we have to use the -cq option instead for
						// better performance?
						try {
							// Create temp file.
							temp = File.createTempFile("eclipse-ccase", ".cmt");
							temp.deleteOnExit();
							BufferedWriter out = new BufferedWriter(
									new FileWriter(temp));
							out.write(command[i]);
							out.close();
							cmd += "-cfile \"" + temp.getAbsolutePath() + "\" ";
						} catch (IOException e) {
							ClearCase.error(ClearCase.ERROR_IO, e, null);
						}
					} else {
						// not comment, or no special char found, great. No
						// special handling necessary.
						if (commentIndex == i) {
							cmd += "-c ";
						}
						if (command[i].contains(" ")
								|| command[i].contains("[")
								|| command[i].contains("]")) {
							cmd += "\"" + command[i] + "\" ";
						} else {
							if (command[i].equals("-c")) {
								// next arg is comment; remember this but do not
								// add the "-c" right here
								commentIndex = i + 1;
							} else {
								cmd += command[i] + " ";
							}
						}
					}
				}
				assertSubprocessRunning(listener);

				// set working directory if needed
				if (workingDir != null) {
					String[] cdCmd = new String[3];
					cdCmd[0] = "cleartool";
					cdCmd[1] = "cd";
					cdCmd[2] = workingDir.getAbsolutePath();
					execute(cdCmd, null, env, listener);
				}

				setCommandFinished(false);
				stdoutReader.reset();
				stderrReader.reset();
				if (ClearCaseCLIImpl.getDebugLevel() > 0) {
					if (workingDir != null) {
						System.out.println(">   exec() in " + workingDir);
					}
					System.out.println(">>> " + cmd);
				}
				if (listener != null) {
					listener.print("Executing cleartool command:");
					listener.printInfo(cmd);
					if (workingDir != null) {
						listener.printInfo("Working Directory: " + workingDir);
					}
					listener.printInfo("");
				}

				process.getOutputStream().write(cmd.getBytes());
				process.getOutputStream().write("\n".getBytes());
				process.getOutputStream().flush();

				synchronized (this) {
					if (stdoutReader.isAlive()) {
						while (!isCommandFinished()) {
							try {
								wait();
							} catch (InterruptedException e) {
							}
						}
					}
				}

				long currentTimeMillis = System.currentTimeMillis();
				long elapsedTimeMillis = currentTimeMillis - start;
				if (ClearCaseCLIImpl.getDebugLevel() > 0) {
					System.out.println("<<  duration: " + elapsedTimeMillis
							+ " ms  s=" + start + "  e=" + currentTimeMillis);
				}
				if (listener != null) {
					listener.printInfo("--> finished, exit status "
							+ getExitValue() + ", duration: "
							+ elapsedTimeMillis + "ms");
				}
			} catch (IOException e) {
				if (e.getMessage().contains("pipe is being closed")) {
					// ok, subprocess died
				} else {
					ClearCase.error(ClearCase.ERROR_IO, e, null);
				}
			} finally {
				execMgr.setOperationListener(null);
			}
			response.setStdOutMsg(getOutput());
			response.setStdErrMsg(getErrorOutput());
			response.setExitValue(exitValue);
			if (temp != null) {
				temp.delete();
			}
		}
		return response;
	}

	/**
	 * @param listener
	 * @throws IOException
	 */
	private void assertSubprocessRunning(OperationListener listener)
			throws IOException {
		if (process == null || stdoutReader == null || !stdoutReader.isAlive()) {
			stdoutReader = new StreamReaderThread(listener, false, this);
			stderrReader = new StreamReaderThread(listener, true, this);
			stdoutReader.setDaemon(true);
			stderrReader.setDaemon(true);
			// Start threads storing the output of the basecommand.
			stdoutReader.start();
			stderrReader.start();
			if (ClearCaseCLIImpl.getDebugLevel() > 0) {
				System.out.println("*** Starting cleartool subprocess ***");
			}
			process = Runtime.getRuntime().exec("cleartool -status");

			if (execMgr == null) {
				execMgr = new ExecutionManagementThread(listener, process);
				execMgr.setDaemon(true);
				execMgr.start();
			} else {
				execMgr.setOperationListener(listener);
			}
			// Set input streams and wakeup reader threads
			stdoutReader.setInputStream(process.getInputStream());
			stderrReader.setInputStream(process.getErrorStream());
		} else {
			execMgr.setOperationListener(listener);
			stdoutReader.setOperationListener(listener);
			stderrReader.setOperationListener(listener);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.commandline.ICommandLauncher#getError()
	 */
	public String[] getErrorOutput() {
		return this.stderrReader.getOutput();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sourceforge.clearcase.commandline.ICommandLauncher#getExitValue()
	 */
	public int getExitValue() {
		return exitValue;
	}

	protected void setExitValue(int ev) {
		exitValue = ev;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sourceforge.clearcase.commandline.ICommandLauncher#getOutput()
	 */
	public String[] getOutput() {
		return this.stdoutReader.getOutput();
	}

}
