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
 *     Tobias Sodergren - configurable job priority
 *******************************************************************************/

package net.sourceforge.eclipseccase;

import org.apache.commons.collections.buffer.PriorityBuffer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

/**
 * The queue for refresh state jobs.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
class StateCacheJobQueue extends Job {

	/** the name of this job */
	private static final String MESSAGE_QUEUE_NAME = Messages
			.getString("StateCacheJobQueue.jobLabel"); //$NON-NLS-1$

	/** the default delay */
	private static final int DEFAULT_DELAY = 150;

	private static final String TRACE_ID = "JobQueue"; //$NON-NLS-1$

	/** the priority buffer */
	private PriorityBuffer queue;

	/** the interrupted state */
	private boolean interrupted = false;

	/** the job manager */
	private final IJobManager jobManager = Job.getJobManager();

	/** the system bundle */
	private final Bundle systemBundle = Platform.getBundle("org.eclipse.osgi"); //$NON-NLS-1$

	/**
	 * Creates a new instance.
	 * 
	 * @param name
	 */
	StateCacheJobQueue() {
		super(MESSAGE_QUEUE_NAME);

		// create underlying priority queue
		this.queue = new PriorityBuffer(400, false);

		// execute as system job if hidden
		setSystem(ClearCasePlugin.isHideRefreshActivity());

		// set priority for long running jobs
		setPriority(ClearCasePlugin.jobQueuePriority());

		// NOT: set the rule to the clearcase engine
		// NOT: setRule(ClearCasePlugin.RULE_CLEARCASE_REFRESH);
		// (if we have such a scheduling rule here, we can't do any
		// refreshLocal() calls from inside this thread. So use a null rule)
		setRule(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.internal.jobs.InternalJob#run(org.eclipse.core.runtime
	 * .IProgressMonitor)
	 */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// synchronized in case build starts during checkCancel
		synchronized (this) {
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;
			// if the system is shutting down, don't update
			if (systemBundle.getState() == Bundle.STOPPING)
				return Status.OK_STATUS;
		}
		IStatus ret;
		if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
			ClearCasePlugin.trace(TRACE_ID, "run(): starting "
					+ System.currentTimeMillis());
		}
		try {
			executePendingJobs(monitor);
			// if the update was successful then it should not be recorded as
			// interrupted
			interrupted = false;
			ret = Status.OK_STATUS;
		} catch (OperationCanceledException e) {
			ret = Status.CANCEL_STATUS;
		} catch (CoreException sig) {
			ret = sig.getStatus();
		}
		// if we have items left in the queue, reschedule a run
		synchronized (queue) {
			if (!queue.isEmpty()) {
				if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
					ClearCasePlugin.trace(TRACE_ID, "run(): reschedule");
				}
				scheduleQueueRun();
			}
		}
		if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
			ClearCasePlugin.trace(TRACE_ID, "run(): end "
					+ System.currentTimeMillis());
		}
		return ret;

	}

	/**
	 * Executes all pending jobs
	 * 
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	private void executePendingJobs(IProgressMonitor monitor)
			throws CoreException, OperationCanceledException {

		try {
			monitor.beginTask(MESSAGE_QUEUE_NAME, queue.size());

			// break from time to time to give time for UI update
			// seems to make no difference? -> disable (use 50000)
			int doneCnt = 0;

			while (!queue.isEmpty() && doneCnt < 50000) {

				if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
					ClearCasePlugin.trace(TRACE_ID, "executePendingJobs: "
							+ queue.size());
				}
				checkCanceled(monitor);

				StateCacheJob job = null;

				// synchronize on the buffer but execute job outside lock
				synchronized (queue) {
					if (!queue.isEmpty()) {
						job = (StateCacheJob) queue.remove();
					}
				}

				// check if buffer was empty
				if (null == job) {
					break;
				}

				// execute job
				if (null != job.getStateCache().getResource()) {
					if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
						ClearCasePlugin.trace(TRACE_ID, "executePendingJobs: "
								+ job.getStateCache().getResource()
										.getFullPath());
					}
					monitor.subTask(Messages
							.getString("StateCacheJobQueue.task.refresh") //$NON-NLS-1$
							+ job.getStateCache().getResource().getFullPath());
					job.execute(new SubProgressMonitor(monitor, 1));
					doneCnt++;
				}
			}
		} finally {
			monitor.done();
		}
		if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
			ClearCasePlugin.trace(TRACE_ID, "executePendingJobs: done");
		}
	}

	/**
	 * Cancel the update if the user has canceled or if an update has been
	 * interrupted.
	 */
	private void checkCanceled(IProgressMonitor monitor) {
		// if the monitor is canceled, throw an exception.
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		// if the system is shutting down, don't build
		if (systemBundle.getState() == Bundle.STOPPING)
			throw new OperationCanceledException();
		// check for request to interrupt the auto-build
		if (isInterrupted())
			throw new OperationCanceledException();
	}

	/**
	 * Schedules the specified job.
	 * <p>
	 * Only high priority jobs will be rescheduled if the same job is already
	 * scheduled.
	 * </p>
	 * 
	 * @param job
	 *            the job to schedule
	 */
	public void schedule(StateCacheJob job) {
		schedule(new StateCacheJob[] { job });
	}

	/**
	 * Schedules the specified jobs.
	 * <p>
	 * Only high priority jobs will be rescheduled if the same job is already
	 * scheduled.
	 * </p>
	 * 
	 * @param job
	 *            the job to schedule
	 */
	public void schedule(StateCacheJob[] jobs) {

		boolean doSchedule = false;

		// synchronize on the buffer
		synchronized (queue) {

			for (int i = 0; i < jobs.length; i++) {
				StateCacheJob job = jobs[i];
				if (queue.contains(job)) {
					if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
						ClearCasePlugin.trace(TRACE_ID,
								"schedule: already in queue "
										+ job.getStateCache().getPath());
					}
					// only reschedule high priority jobs
					if (StateCacheJob.PRIORITY_HIGH == job.getPriority()) {
						if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
							ClearCasePlugin.trace(TRACE_ID,
									"schedule: remove+add ");
						}
						// reschedule
						queue.remove(job);
						queue.add(job);
						doSchedule = true;
					}
				} else {
					if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
						ClearCasePlugin.trace(TRACE_ID, "schedule: adding "
								+ job.getStateCache().getPath());
					}

					queue.add(job);
					doSchedule = true;
				}
			}
		}

		// schedule a queue "run"
		if (doSchedule) {
			// interrupt ongoing refreshes
			interrupt();
			scheduleQueueRun();
		}
	}

	/**
	 * Schedules this queue job.
	 */
	synchronized void scheduleQueueRun() {
		interrupted = false;
		int state = getState();
		switch (state) {
		case Job.SLEEPING:
			if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
				ClearCasePlugin.trace(TRACE_ID,
						"scheduleQueueRun SLEEPING -> wakeUp() "
								+ System.currentTimeMillis());
			}
			wakeUp(DEFAULT_DELAY);
			break;
		case NONE:
			if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
				ClearCasePlugin.trace(TRACE_ID,
						"scheduleQueueRun NONE -> schedule() "
								+ System.currentTimeMillis());
			}
			schedule(DEFAULT_DELAY);
			break;
		case RUNNING:
			if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
				ClearCasePlugin.trace(TRACE_ID,
						"scheduleQueueRun RUNNING -> schedule() "
								+ System.currentTimeMillis());
			}
			schedule(DEFAULT_DELAY);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
	 */
	@Override
	public boolean belongsTo(Object family) {
		return ClearCasePlugin.FAMILY_CLEARCASE_OPERATION == family;
	}

	/**
	 * Stops the job.
	 * 
	 * @param clean
	 *            indicates if all pending refresh jobs should be deleted
	 * @return <code>false</code> if the job is currently running (and thus may
	 *         not respond to cancelation), and <code>true</code> in all other
	 *         cases.
	 * @see Job#cancel()
	 */
	public boolean cancel(boolean clean) {
		if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
			ClearCasePlugin.trace(TRACE_ID, "cancel() " + clean);
		}
		boolean canceled = cancel();
		if (clean) {
			synchronized (queue) {
				queue.clear();
			}
		}
		return canceled;
	}

	/**
	 * Another thread is attempting to modify the workspace. Flag the update job
	 * as interrupted so that it will cancel and reschedule itself
	 */
	synchronized void interrupt() {
		if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
			ClearCasePlugin.trace(TRACE_ID, "interrupt(), old i-state: "
					+ interrupted);
		}
		// if already interrupted, do nothing
		if (interrupted)
			return;
		switch (getState()) {
		case NONE:
			return;
		case WAITING:
			if (ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
				ClearCasePlugin.trace(TRACE_ID, "going to sleep");
			}
			// put the job to sleep if it is waiting to run
			interrupted = !sleep();
			break;
		case RUNNING:
			// make sure we don't interrupt ourself
			interrupted = jobManager.currentJob() != this;
			if (interrupted && ClearCasePlugin.DEBUG_UPDATE_QUEUE) {
				ClearCasePlugin
						.trace(
								TRACE_ID,
								"update job was interrupted: " + Thread.currentThread().getName()); //$NON-NLS-1$
				// new Exception().fillInStackTrace().printStackTrace();
			}
			break;
		}
	}

	synchronized boolean isInterrupted() {
		if (interrupted)
			return true;
		// check if another job is blocked by the build job
		if (isBlocking()) {
			interrupted = true;
		}
		return interrupted;
	}

	/**
	 * Indicates if the job queue is empty
	 * 
	 * @return <code>true</code> if empty
	 */
	public boolean isEmpty() {
		// do not synchronize
		return queue.isEmpty();
	}
}