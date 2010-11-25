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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * A job for refreshing the state of a resource.
 * 
 * @author Gunnar Wagenknecht (g.wagenknecht@planet-wagenknecht.de)
 */
class StateCacheJob implements Comparable<StateCacheJob> {

	/** default priority */
	static final int PRIORITY_DEFAULT = 0;

	/** high priority */
	static final int PRIORITY_HIGH = 1000;

	/** the state cache to refresh */
	private StateCache stateCache;

	/** the priority (higher value means higher priority) */
	private int priority;

	/**
	 * Creates a new job with default priority.
	 * 
	 * @param cache
	 *            the state cache to refresh
	 */
	StateCacheJob(StateCache cache) {
		this(cache, PRIORITY_DEFAULT);
	}

	/**
	 * Creates a njob with the specified priority.
	 * 
	 * @param cache
	 * @param jobPriority
	 */
	public StateCacheJob(StateCache cache, int jobPriority) {
		this.stateCache = cache;
		this.priority = jobPriority;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(StateCacheJob o) {
		if (this == o)
			return 0;

		// will throw ClassCastException which is ok here
		StateCacheJob other = (StateCacheJob) o;

		// use priority delta
		int priorityDelta = this.priority - other.priority;

		if (priorityDelta > 0)
			// our priority is higher than the other priority
			// we are greater than the other
			return 1;

		if (priorityDelta < 0)
			// our priority is smaller than the other priority
			// we are lesser than the other
			return -1;

		// equal priority
		return 0;
	}

	/**
	 * Returns the stateCache.
	 * 
	 * @return returns the stateCache
	 */
	StateCache getStateCache() {
		return stateCache;
	}

	/**
	 * Returns the priority.
	 * 
	 * @return returns the priority
	 */
	int getPriority() {
		return priority;
	}

	/**
	 * Executes this job
	 * <p>
	 * This method should not be called by clients. It is called by the
	 * {@link StateCacheJobQueue}.
	 * </p>
	 * 
	 * @param monitor
	 * @throws CoreException
	 *             in case of problems
	 * @throws OperationCanceledException
	 *             if the operation was canceled
	 */
	void execute(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		getStateCache().doUpdate(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		if (null == getStateCache())
			return super.hashCode();

		return getStateCache().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (null == getStateCache())
			return super.equals(obj);
		if (null == obj || this.getClass() != obj.getClass())
			return false;

		StateCacheJob other = (StateCacheJob) obj;
		return getStateCache().equals(other.getStateCache());
	}

	/**
	 * Schedules this job with the specified priority.
	 * 
	 * @param jobPriority
	 *            the priority
	 * @see #PRIORITY_DEFAULT
	 * @see #PRIORITY_HIGH
	 */
	public void schedule(int jobPriority) {
		priority = jobPriority;
		StateCacheJobQueue queue = StateCacheFactory.getInstance()
				.getJobQueue();
		queue.schedule(this);
	}
}