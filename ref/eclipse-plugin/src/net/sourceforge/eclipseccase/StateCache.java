/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * 		Matthew Conway - initial API and implementation 
 * 		IBM Corporation - concepts and ideas from Eclipse 
 *      Gunnar Wagenknecht - new features, enhancements and bug fixes
 ******************************************************************************/
package net.sourceforge.eclipseccase;

import java.io.IOException;
import java.io.Serializable;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.Team;

public class StateCache implements Serializable {

	static final long serialVersionUID = -7439899000320633901L;

	private String osPath;

	private String workspaceResourcePath;

	transient IResource resource;

	transient long updateTimeStamp = IResource.NULL_STAMP;

	int flags = 0;

	String version;

	StateCache(IResource resource) {
		if (null == resource)
			throw new IllegalArgumentException("Resource must not be null!"); //$NON-NLS-1$

		this.resource = resource;

		IPath location = resource.isAccessible() ? resource.getLocation()
				: null;
		if (location != null) {
			osPath = location.toOSString();
		} else {
			// resource has been invalidated in the workspace since request was
			// queued, so ignore update request.
			osPath = null;
		}
	}

	private static final String TRACE_ID = "StateCache"; //$NON-NLS-1$

	String symbolicLinkTarget;

	// flags

	private static final int IS_ELEMENT = 0x1;

	private static final int CHECKED_OUT = 0x2;

	private static final int SNAPSHOT = 0x4;

	private static final int HIJACKED = 0x8;

	private static final int CHECKED_OUT_OTHER_VIEW = 0x10;

	private static final int SYM_LINK = 0x20;

	private static final int SYM_LINK_TARGET_VALID = 0x40;

	private static final int INSIDE_VIEW = 0x80;

	private static final int DERIVED_OBJECT = 0x100;

	static final int VP_STATE_VERIFIED = 0x4000;

	/**
	 * Schedules a state update.
	 * 
	 * @param invalidate
	 */
	public void updateAsync(boolean invalidate) {
		updateAsync(invalidate, false);
	}

	/**
	 * Schedules a state update.
	 * 
	 * @param invalidate
	 * @param useHighPriority
	 * @param statusCollector
	 *            Collector that is responsible for getting status information
	 *            for resources.
	 */
	private void updateAsync(boolean invalidate, boolean useHighPriority) {
		if (invalidate) {
			if (!isUninitialized()) {
				// synchronize access
				synchronized (this) {
					updateTimeStamp = IResource.NULL_STAMP;
				}
				if (ClearCasePlugin.DEBUG_STATE_CACHE) {
					ClearCasePlugin.trace(TRACE_ID,
							"invalidating " + this.getPath()); //$NON-NLS-1$
				}
				// fireing state change (the update was forced)
				// StateCacheFactory.getInstance().fireStateChanged(this.resource);
			}
		}
		StateCacheJob job;
		job = new StateCacheJob(this);
		job.schedule(useHighPriority ? StateCacheJob.PRIORITY_HIGH
				: StateCacheJob.PRIORITY_DEFAULT);
	}

	/**
	 * Updates the state.
	 * 
	 * @param monitor
	 * @throws CoreException
	 * @throws OperationCanceledException
	 */
	void doUpdate(IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			monitor
					.beginTask(
							Messages.getString("StateCache.updating") + getResource(), 10); //$NON-NLS-1$
			doUpdate();
			monitor.worked(10);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Updates the state. Eventually calls the engine's getElementState() (which
	 * execs cleartool)
	 */
	public void doUpdate() {
		doUpdate((ClearCaseElementState) null);
	}

	/**
	 * Updates the state. Calls the engine's getElementState() (which execs
	 * cleartool) if no givenState
	 * 
	 * @param givenState
	 *            the ClearCaseElementState to update from, or null if the
	 *            engine shall be queried for current state
	 */
	void doUpdate(ClearCaseElementState givenState) {
		boolean changed = isUninitialized();
		//ClearCasePlugin.trace(TRACE_ID, "XXX doUpdate: " + resource); //$NON-NLS-1$

		IPath location = resource.getLocation();
		if (location == null) {
			// resource has been invalidated in the workspace since request was
			// queued, so ignore update request.
			if (ClearCasePlugin.DEBUG_STATE_CACHE) {
				ClearCasePlugin.trace(TRACE_ID,
						"not updating - invalid resource: " //$NON-NLS-1$
								+ resource);
			}
			return;
		}

		// only synchronize here
		synchronized (this) {

			osPath = location.toOSString();

			if (ClearCasePlugin.DEBUG_STATE_CACHE) {
				ClearCasePlugin.trace(TRACE_ID, "updating " + resource); //$NON-NLS-1$
				ClearCasePlugin.trace("[StateCache] update in thread: "
						+ Thread.currentThread().getName()
						+ " ID=" + Thread.currentThread().getId()); //$NON-NLS-1$
			}

			if (!resource.isAccessible()) {
				// file/dir is present on disk, but not available in workspace.
				// This can happen if "automatic refresh" in workspace prefs is
				// disabled, and we create a resource outside Eclipse. A
				// subsequent refresh in the ViewPrivate view picks up the newly
				// created file, which in turn triggers the update of the state
				// cache. In this case we want to incorporate the file
				// automatically into the workspace.
				if (ClearCasePlugin.DEBUG_STATE_CACHE) {
					ClearCasePlugin.trace(TRACE_ID,
							"not accessible, refreshing in WS: " + resource); //$NON-NLS-1$
				}
				// resource does not exist in workspace (refresh needed)
				flags = 0;
				version = null;
				symbolicLinkTarget = null;
				updateTimeStamp = IResource.NULL_STAMP;
				scheduleRefreshLocal(false);
				// when resource is added to workspace, a resource change
				// notification is run, which in turn triggers a state update.
				// No need to continue now
				return;
			} else {

				// check the global ignores from Team (includes derived
				// resources)
				if (!Team.isIgnoredHint(resource)) {
					ClearCaseElementState newState = null;
					if (givenState != null) {
						newState = givenState;
					}

					if (newState == null
							&& ClearCasePlugin
									.isUnneededChildrenRefreshPrevented()
							&& resource.isSynchronized(0)) {
						// check parent for CC state, don't update if parent is
						// not a CC element
						IResource parent = resource.getParent();
						if (null != parent && !(parent instanceof IProject)
								&& !(parent instanceof IWorkspaceRoot)) {
							StateCache parentCache = StateCacheFactory
									.getInstance().getWithNoUpdate(parent);
							if (!parentCache.isUninitialized()
									&& !parentCache.isClearCaseElement()) {
								// parent is no CC element, so don't call CC for
								// state
								newState = new ClearCaseElementState(osPath,
										ClearCase.VIEW_PRIVATE);
							}
							if (parentCache.isUninitialized()
									&& parentCache.isClearCaseElement()) {
								// schedule a high priority refresh, so that
								// further
								// elements of same parent get a real result
								// from
								// cache
								// TODO check, does this really work?
								StateCacheFactory.getInstance().refreshState(
										new IResource[] { parent },
										StateCacheJob.PRIORITY_HIGH);
							}
						}
					}

					if (null == newState) {
						newState = ClearCasePlugin.getEngine().getElementState(
								osPath);
					}

					// Fix for Bug 2509230.
					boolean isInsideSnapshotView = ClearCaseProvider
							.isSnapshotView(ClearCaseProvider
									.getViewName(resource));

					if (newState != null) {
						if (newState.isMissing()) {
							scheduleRefreshLocal(true);
							return;
						}
						boolean newIsElement = newState.isElement();
						changed |= newIsElement != this.isClearCaseElement();
						setFlag(IS_ELEMENT, newIsElement);

						boolean newInsideView = !newState.isOutsideVob();
						changed |= newInsideView != this.isInsideView();
						setFlag(INSIDE_VIEW, newInsideView);

						boolean newIsSymbolicLink = newState.isLink();
						changed |= newIsSymbolicLink != this.isSymbolicLink();
						setFlag(SYM_LINK, newIsSymbolicLink);

						boolean newIsDerivedObject = newState.isDerivedObject();
						changed |= newIsDerivedObject != this.isDerivedObject();
						setFlag(DERIVED_OBJECT, newIsDerivedObject);

						boolean newIsCheckedOut = newState.isCheckedOut();
						if (!newIsSymbolicLink) {
							// for symlinks the checkout state is calculated
							// later
							changed |= newIsCheckedOut != this.isCheckedOut();
							setFlag(CHECKED_OUT, newIsCheckedOut);
						}

						changed |= isInsideSnapshotView != this.isSnapShot();
						setFlag(SNAPSHOT, isInsideSnapshotView);

						boolean newIsHijacked = newState.isHijacked();
						changed |= newIsHijacked != this.isHijacked();
						setFlag(HIJACKED, newIsHijacked);

						boolean newIsEdited = false;
						changed |= newIsEdited != this.isEdited();
						setFlag(CHECKED_OUT_OTHER_VIEW, newIsEdited);

						String newVersion = newState.version;
						changed |= newVersion == null ? null != this.version
								: !newVersion.equals(this.version);
						this.version = newVersion;

						if (newIsSymbolicLink) {
							changed |= updateSymlinkState(newState.linkTarget);
						} else if (null != this.symbolicLinkTarget) {
							this.symbolicLinkTarget = null;
							setFlag(SYM_LINK_TARGET_VALID, false);
							changed = true;
						}

						setFlag(VP_STATE_VERIFIED, true);
					}// End newState !=null

				} else {
					// resource is ignored by Team plug-ins
					flags = 0;
					version = null;
					symbolicLinkTarget = null;
					changed = false;
					if (ClearCasePlugin.DEBUG_STATE_CACHE) {
						ClearCasePlugin.trace(TRACE_ID,
								"resource must be ignored: " //$NON-NLS-1$
										+ resource);
					}
				}

				updateTimeStamp = resource.getModificationStamp();
			}

		}

		// fire state change (lock must be released prior)
		if (changed) {
			if (ClearCasePlugin.DEBUG_STATE_CACHE) {
				ClearCasePlugin.trace(TRACE_ID, "updated " + this); //$NON-NLS-1$
			}
			StateCacheFactory.getInstance().fireStateChanged(this.resource);
		} else {
			// no changes
			if (ClearCasePlugin.DEBUG_STATE_CACHE) {
				ClearCasePlugin.trace(TRACE_ID, "  no changes detected"); //$NON-NLS-1$ 
			}
		}
	}

	private void scheduleRefreshLocal(final boolean refreshParent) {
		Job localRefreshJob = new Job(
				"Refreshing state for " + resource.toString()) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					if (refreshParent && resource.getParent() != null) {
						resource.getParent().refreshLocal(IResource.DEPTH_ONE,
								new NullProgressMonitor());
					} else {
						resource.refreshLocal(IResource.DEPTH_ZERO,
								new NullProgressMonitor());
					}
				} catch (CoreException e) {
				}
				return Status.OK_STATUS;
			}
		};
		localRefreshJob.setPriority(Job.SHORT);
		localRefreshJob.schedule();
	}

	/**
	 * Update all internal values of the StateCache by examining the
	 * corresponding symlink target.
	 * 
	 * @param targetPath
	 *            string representation of symlink target
	 * @return true if internal state has changed
	 */
	private boolean updateSymlinkState(String targetPath) {
		boolean changed = false;
		if (null != targetPath && targetPath.trim().length() == 0) {
			targetPath = null;
		}
		changed |= (null == targetPath) ? (null != this.symbolicLinkTarget)
				: (!targetPath.equals(this.symbolicLinkTarget));
		this.symbolicLinkTarget = targetPath;

		// TODO calculate IsTargetValid state of symlink (Achim 2010 2 5)
		boolean newIsTargetValid = true;

		// TODO calculate checkout state of target (Achim 2010 2 5)
		boolean targetIsCheckedOut = false; // newState.isCheckedOut();
		// get our provider
		ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
		if (p != null) {
			StateCache target = p.getFinalTargetElement(this);
			targetIsCheckedOut = (target != null && target.isCheckedOut());
		} else {
			newIsTargetValid = false;
		}
		changed |= targetIsCheckedOut != this.isCheckedOut();
		setFlag(CHECKED_OUT, targetIsCheckedOut);

		changed |= newIsTargetValid != this.isSymbolicLinkTargetValid();
		setFlag(SYM_LINK_TARGET_VALID, newIsTargetValid);
		return changed;
	}

	/**
	 * Indicates if the underlying resource is a ClearCase element.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isClearCaseElement() {
		return getFlag(IS_ELEMENT);
	}

	/**
	 * Indicates if the underlying resource is a view-privat resource (inside
	 * the VOB area).
	 * 
	 * @return Returns a boolean
	 */
	public boolean isViewprivate() {
		return (!getFlag(IS_ELEMENT) && getFlag(INSIDE_VIEW));
	}

	/**
	 * Gets the isCheckedOut().
	 * 
	 * @return Returns a boolean
	 */
	public boolean isCheckedOut() {
		return getFlag(CHECKED_OUT);
	}

	/**
	 * Gets the isDirty.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isDirty() {
		if (null == resource)
			return false;

		// performance improve: if not checked out it is not dirty
		// wrong : it can be hijacked
		// if (!isCheckedOut()) return false;

		// this is too expensive
		// try {
		// return ClearCasePlugin.getEngine().isDifferent(osPath);
		// } catch (RuntimeException ex) {
		// ClearCasePlugin.log(IStatus.ERROR,
		// "Could not determine element dirty state of "
		// + osPath
		// + ": "
		// + (null != ex.getCause() ? ex.getCause()
		// .getMessage() : ex.getMessage()), ex);
		// return false;
		// }

		return resource.getModificationStamp() != updateTimeStamp;
	}

	/**
	 * Indicates if the resource is edited by someone else.
	 * 
	 * @return Returns a boolean
	 */
	public boolean isEdited() {
		return getFlag(CHECKED_OUT_OTHER_VIEW);
	}

	/**
	 * Returns the osPath.
	 * 
	 * @return String
	 */
	public String getPath() {
		return osPath;
	}

	/**
	 * Returns the version.
	 * 
	 * @return String
	 */
	public String getVersion() {
		return null == version ? "" : version; //$NON-NLS-1$
	}

	/**
	 * Returns the predecessor version.
	 * 
	 * @return String
	 */
	public String getPredecessorVersion() {
		String predecessorVersion = ClearCasePlugin.getEngine()
				.getPreviousVersion(resource.getLocation().toOSString());
		return predecessorVersion;
	}

	/**
	 * Returns the isUninitialized().
	 * 
	 * @return boolean
	 */
	public boolean isUninitialized() {
		// always ignore Team-ignore resources
		if (Team.isIgnoredHint(resource))
			return false;

		// check if we have a timestamp
		return IResource.NULL_STAMP == updateTimeStamp;
	}

	/**
	 * Returns the isHijacked().
	 * 
	 * @return boolean
	 */
	public boolean isHijacked() {
		return getFlag(HIJACKED);
	}

	/**
	 * Returns the isSnapShot().
	 * 
	 * @return boolean
	 */
	public boolean isSnapShot() {
		return getFlag(SNAPSHOT);
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		// special handling for resource
		if (null != resource) {
			// make sure we only save states for real resources
			if (resource.isAccessible()) {
				this.workspaceResourcePath = resource.getFullPath().toString();
			} else {
				this.workspaceResourcePath = null;
			}
		}
		out.defaultWriteObject();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();

		// restore resource
		if (null != workspaceResourcePath) {
			// determine resource
			IPath path = new Path(workspaceResourcePath);
			resource = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(path);
			if (resource != null && resource.isAccessible()) {
				IPath location = resource.getLocation();
				if (location != null) {
					osPath = location.toOSString();
				} else {
					// resource has been invalidated in the workspace since
					// request was
					// queued, so ignore update request.
					osPath = null;
				}
			} else {
				// invalid resource
				resource = null;
				osPath = null;
				workspaceResourcePath = null;
			}
		} else {
			// invalid resource
			resource = null;
			osPath = null;
			workspaceResourcePath = null;
		}
	}

	/**
	 * Returns the resource.
	 * 
	 * @return IResource
	 */
	public IResource getResource() {
		return resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer("StateCache "); //$NON-NLS-1$
		toString.append(resource);
		toString.append(": "); //$NON-NLS-1$
		if (isUninitialized()) {
			toString.append("not initialized"); //$NON-NLS-1$
		} else if (!isClearCaseElement()) {
			toString.append("no clearcase element"); //$NON-NLS-1$
			if (!isInsideView()) {
				toString.append(", outside VOB"); //$NON-NLS-1$
			}
		} else {
			if (version != null)
				toString.append(version);

			if (isSymbolicLink()) {
				toString.append(" [SYMBOLIC LINK ("); //$NON-NLS-1$
				toString.append(symbolicLinkTarget);
				toString.append(")]"); //$NON-NLS-1$
			}

			if (isCheckedOut()) {
				toString.append(" [CHECKED OUT]"); //$NON-NLS-1$
			}

			if (isHijacked()) {
				toString.append(" [HIJACKED]"); //$NON-NLS-1$
			}

			if (isDerivedObject()) {
				toString.append(" [DERIVEDOBJ]"); //$NON-NLS-1$
			}

			if (isSnapShot()) {
				toString.append(" [SNAPSHOT]"); //$NON-NLS-1$
			}
		}

		return toString.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (null == resource)
			return 0;

		return resource.hashCode();
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

		if (null == obj || StateCache.class != obj.getClass())
			return false;

		if (null == resource)
			return null == ((StateCache) obj).resource;

		return resource.equals(((StateCache) obj).resource);
	}

	/**
	 * @return
	 */
	public boolean isSymbolicLink() {
		return getFlag(SYM_LINK);
	}

	public boolean isDerivedObject() {
		return getFlag(DERIVED_OBJECT);
	}

	public boolean isVpStateVerified() {
		return getFlag(VP_STATE_VERIFIED);
	}

	public void setVpStateVerified() {
		setFlag(VP_STATE_VERIFIED, true);
	}

	/**
	 * Returns the symbolicLinkTarget.
	 * 
	 * @return returns the symbolicLinkTarget
	 */
	public String getSymbolicLinkTarget() {
		return null == symbolicLinkTarget ? "" : symbolicLinkTarget; //$NON-NLS-1$
	}

	/**
	 * @return
	 */
	public boolean isSymbolicLinkTargetValid() {
		return getFlag(SYM_LINK_TARGET_VALID);
	}

	/**
	 * Indicates if the resource is within a ClearCase view.
	 * 
	 * @return
	 */
	public boolean isInsideView() {
		return getFlag(INSIDE_VIEW);
	}

	/**
	 * Returns <code>true</code> if the specified flag is set.
	 * 
	 * @param flag
	 * @return <code>true</code> if the specified flag is set
	 */
	boolean getFlag(int flag) {
		return 0 != (flags & flag);
	}

	/**
	 * Sets the flag to the specified value.
	 * 
	 * @param flag
	 * @param value
	 */
	void setFlag(int flag, boolean value) {
		if (value) {
			flags |= flag;
		} else {
			flags &= ~flag;
		}
	}

}