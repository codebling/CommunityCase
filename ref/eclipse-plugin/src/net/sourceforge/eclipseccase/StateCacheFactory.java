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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.eclipseccase.tools.XMLWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author abu
 * 
 */
public class StateCacheFactory implements ISaveParticipant,
		IResourceChangeListener {

	/** trace id */
	private static final String TRACE_STATECACHEFACTORY = "StateCacheFactory"; //$NON-NLS-1$

	private static final String SAVE_FILE_NAME = "statecache"; //$NON-NLS-1$

	/** the singleton instance */
	private static StateCacheFactory instance = new StateCacheFactory();

	/** maps resources to caches */
	Map<IResource, StateCache> cacheMap = new HashMap<IResource, StateCache>();

	/** the listeners */
	private List<IResourceStateListener> listeners = new ArrayList<IResourceStateListener>();

	/**
	 * Hidden constructor.
	 */
	private StateCacheFactory() {
		// super
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return
	 */
	public static StateCacheFactory getInstance() {
		return instance;
	}

	/**
	 * Adds a state change listener.
	 * 
	 * @param listener
	 */
	public void addStateChangeListerer(IResourceStateListener listener) {
		if (null != listener) {
			synchronized (listeners) {
				if (!listeners.contains(listener)) {
					listeners.add(listener);
				}
			}
		}
	}

	/**
	 * Removes a state change listener.
	 * 
	 * @param listener
	 * @return
	 */
	public boolean removeStateChangeListerer(IResourceStateListener listener) {
		if (null != listener) {
			synchronized (listeners) {
				return listeners.remove(listener);
			}
		}
		return false;
	}

	/**
	 * Fires a state change for the specified state cache.
	 * 
	 * @param stateCache
	 */
	public void fireStateChanged(IResource resource) {

		if (operationCounter > 0) {

			// only queue if there are ongoing operations
			if (null == resource)
				return;
			synchronized (queuedEvents) {
				queuedEvents.addLast(resource);
			}
		} else {

			// fire event or queued events
			Object[] currentListeners = null;
			synchronized (listeners) {
				currentListeners = listeners.toArray();
			}

			if (null != currentListeners) {

				IResource[] events = null;

				if (null == resource) {

					// fire all pending changes
					synchronized (queuedEvents) {
						events = queuedEvents
								.toArray(new IResource[queuedEvents.size()]);
						queuedEvents.clear();
					}
				} else {

					// fire only the given change
					events = new IResource[] { resource };
				}

				if (null != events && events.length > 0) {
					for (int j = 0; j < currentListeners.length; j++) {
						((IResourceStateListener) currentListeners[j])
								.resourceStateChanged(events);
					}
				}
			}
		}
	}

	/** a list of events queued during long running operations */
	private LinkedList<IResource> queuedEvents = new LinkedList<IResource>();

	/** the operation counter */
	private int operationCounter = 0;

	/** indicates if the state cache is loaded */
	private boolean isStateCacheLoaded = false;

	/**
	 * Starts an operation.
	 */
	public void operationBegin() {
		synchronized (this) {
			operationCounter++;
		}
	}

	/**
	 * Ends an operation.
	 */
	public void operationEnd() {
		boolean fire = false;
		synchronized (this) {
			if (operationCounter == 0)
				return;

			operationCounter--;

			fire = operationCounter == 0;
		}

		if (fire) {
			fireStateChanged(null);
		}
	}

	/**
	 * Indicates if the state of the specified resource is uninitialized.
	 * 
	 * @param resource
	 * @return <code>true</code> if uninitialized
	 */
	public boolean isUninitialized(IResource resource) {
		StateCache cache = cacheMap.get(resource);
		return null == cache || cache.isUninitialized();
	}

	/**
	 * Returns the state cache for the specified resource.
	 * 
	 * @param resource
	 * @return the state cache for the specified resource
	 */
	public synchronized StateCache get(IResource resource) {
		StateCache cache = getWithNoUpdate(resource);
		// schedule update if necessary
		if (isInitialized() && cache.isUninitialized()) {
			cache.updateAsync(false);
		}
		return cache;
	}

	/**
	 * Gets a {@link StateCache} instance without updateing the cache status.
	 * 
	 * @param resource
	 *            The resource for which to get a {@link StateCache} instance.
	 * @return A {@link StateCache} instance.
	 */
	public StateCache getWithNoUpdate(IResource resource) {
		StateCache cache = cacheMap.get(resource);
		if (cache == null) {
			cache = new StateCache(resource);
			cacheMap.put(resource, cache);
		}
		return cache;
	}

	public Iterable<IResource> getContainedResources() {
		return cacheMap.keySet();
	}

	/**
	 * Reset the VP_STATE_VERIFIED flag for all entries. Used together with
	 * refreshAllUnverifiedStates() to detect resources that are out of sync
	 * with the real CC state.
	 * 
	 * Before the real CC state for the view is gathered (with lsprivate,
	 * lsco...) resetVerifiyStates() is called once. While processing the CC
	 * output, the VP_STATE_VERIFIED flag is set for all verified entries.
	 * Later, refreshAllUnverifiedStates() gets called which schedules a refresh
	 * for all entries that were not verified.
	 * 
	 * @see StateCacheFactory#refreshAllUnverifiedStates(boolean, boolean,
	 *      boolean)
	 */
	public void resetVerifiyStates() {
		for (StateCache s : cacheMap.values()) {
			s.setFlag(StateCache.VP_STATE_VERIFIED, false);
		}
	}

	/**
	 * Schedule a state refresh for all entries with a cleared VP_STATE_VERIFIED
	 * flag.
	 * 
	 * @see StateCacheFactory#resetVerifiyStates()
	 * @param doCheckedout
	 * @param doNew
	 * @param doHijacked
	 */
	public void refreshAllUnverifiedStates(boolean doCheckedout, boolean doNew,
			boolean doHijacked) {
		for (StateCache s : cacheMap.values()) {
			if (!s.isVpStateVerified()) {
				if (doCheckedout && s.isCheckedOut()) {
					if (ClearCasePlugin.DEBUG_STATE_CACHE) {
						ClearCasePlugin.trace(TRACE_STATECACHEFACTORY,
								"refetch unverified CO: " + s.getPath()); //$NON-NLS-1$
					}
					s.updateAsync(true);
				} else if (doNew && s.isViewprivate()) {
					if (ClearCasePlugin.DEBUG_STATE_CACHE) {
						ClearCasePlugin.trace(TRACE_STATECACHEFACTORY,
								"refetch unverified viewpriv: " + s.getPath()); //$NON-NLS-1$
					}
					s.updateAsync(true);
				} else if (doHijacked && s.isHijacked()) {
					if (ClearCasePlugin.DEBUG_STATE_CACHE) {
						ClearCasePlugin.trace(TRACE_STATECACHEFACTORY,
								"refetch unverified hijacked: " + s.getPath()); //$NON-NLS-1$
					}
					s.updateAsync(true);
				}
			}
		}
	}

	/**
	 * Removes the state cache for the specified resource including all its
	 * direct and indirect members.
	 * 
	 * @param resource
	 */
	public void remove(IResource resource) {
		if (resource.isAccessible()) {
			try {
				resource.accept(new IResourceVisitor() {

					public boolean visit(IResource childResource)
							throws CoreException {
						switch (childResource.getType()) {
						case IResource.PROJECT:
						case IResource.FOLDER:
							removeSingle(childResource);
							return true;
						default:
							removeSingle(childResource);
							return false;
						}
					}
				});
			} catch (CoreException ex) {
				// not accessible
			}
		}
		removeSingle(resource);
	}

	/**
	 * Removes the state cache for the specified resource.
	 * 
	 * @param resource
	 */
	void removeSingle(IResource resource) {
		if (cacheMap.containsKey(resource)) {
			synchronized (cacheMap) {
				cacheMap.remove(resource);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core
	 * .resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
		int previousSaveNumber = context.getPreviousSaveNumber();
		String oldFileName = SAVE_FILE_NAME
				+ Integer.toString(previousSaveNumber);
		File file = ClearCasePlugin.getInstance().getStateLocation().append(
				oldFileName).toFile();
		file.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse
	 * .core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
		// prepareToSave
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core
	 * .resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {
		// rollback
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources
	 * .ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		switch (context.getKind()) {

		case ISaveContext.FULL_SAVE:
			try {
				int saveNumber = context.getSaveNumber();
				String saveFileName = SAVE_FILE_NAME
						+ Integer.toString(saveNumber);
				IPath statePath = ClearCasePlugin.getInstance()
						.getStateLocation().append(saveFileName);

				// save state cache
				// ObjectOutputStream os = new ObjectOutputStream(
				// new FileOutputStream(statePath.toFile()));
				// Collection serList = new LinkedList(cacheMap.values());
				// os.writeObject(serList);
				// os.flush();
				// os.close();
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(statePath.toFile()));
				try {
					writeStateCache(os);
					os.flush();
				} finally {
					os.close();
				}
				context.map(new Path(SAVE_FILE_NAME), new Path(saveFileName));
				context.needSaveNumber();
			} catch (IOException ex) {
				throw new CoreException(new Status(IStatus.WARNING,
						ClearCasePlugin.PLUGIN_ID, TeamException.IO_FAILED,
						"Could not persist state cache", ex)); //$NON-NLS-1$
			}
			break;

		case ISaveContext.PROJECT_SAVE:
		case ISaveContext.SNAPSHOT:
			break;
		}
	}

	/** xml element name */
	static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$

	/** xml element name */
	static final String TAG_STATES = "states"; //$NON-NLS-1$

	/** xml attribute name */
	static final String ATTR_PATH = "path"; //$NON-NLS-1$

	/** xml attribute name */
	static final String ATTR_VERSION = "version"; //$NON-NLS-1$

	/** xml attribute name */
	static final String ATTR_TIME_STAMP = "timeStamp"; //$NON-NLS-1$

	/** xml attribute name */
	static final String ATTR_STATE = "state"; //$NON-NLS-1$

	/** xml attribute name */
	static final String ATTR_SYMLINK_TARGET = "symlinkTarget"; //$NON-NLS-1$

	/** the version */
	static final String STATE_CACHE_VERSION = "20040617_104400_GMT+0200"; //$NON-NLS-1$

	/**
	 * Writes the comment history to the specified writer.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void writeStateCache(OutputStream os) throws IOException {

		// create XML writer
		XMLWriter writer = new XMLWriter(os);
		Set<IResource> knownResource = cacheMap.keySet();

		// get and sort resources
		IResource[] resources = knownResource
				.toArray(new IResource[knownResource.size()]);
		Arrays.sort(resources, new Comparator<Object>() {

			public int compare(Object o1, Object o2) {
				return ((IResource) o1).getFullPath().toString().compareTo(
						((IResource) o2).getFullPath().toString());
			}
		});

		// start root tag
		HashMap<String, String> attributes = new HashMap<String, String>(1);
		attributes.put(ATTR_VERSION, STATE_CACHE_VERSION);
		writer.startTag(TAG_STATES, attributes, true);

		// write resource tags
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			// only persist state of initialized, existing and non derived
			// resources
			if (isUninitialized(resource) || !resource.exists()
					|| resource.isDerived()) {
				continue;
			}
			StateCache cache = get(resource);
			attributes = new HashMap<String, String>(5);
			attributes.put(ATTR_PATH, resource.getFullPath().toString());
			attributes.put(ATTR_STATE, Integer.toString(cache.flags));
			attributes.put(ATTR_TIME_STAMP, Long
					.toString(cache.updateTimeStamp));
			if (null != cache.version) {
				attributes.put(ATTR_VERSION, cache.version);
			}
			if (null != cache.symbolicLinkTarget) {
				attributes.put(ATTR_SYMLINK_TARGET, cache.symbolicLinkTarget);
			}
			writer.startAndEndTag(TAG_RESOURCE, attributes, true);
		}

		// finish root TAG
		writer.endTag(TAG_STATES);
		writer.flush();
	}

	/**
	 * Loads the state cache from the specified context.
	 * 
	 * @param context
	 */
	void load(ISavedState context) {
		try {
			if (context != null) {
				String saveFileName = context.lookup(new Path(SAVE_FILE_NAME))
						.toString();
				File stateFile = ClearCasePlugin.getInstance()
						.getStateLocation().append(saveFileName).toFile();
				if (stateFile.exists()) {
					try {
						operationBegin();
						readStateCache(stateFile);
					} finally {
						operationEnd();
					}
				}
			}
		} catch (Exception ex) {
			ClearCasePlugin
					.log(
							IStatus.WARNING,
							"Could not load saved clearcase state cache, resetting cache", //$NON-NLS-1$
							ex);
		} finally {
			synchronized (this) {
				isStateCacheLoaded = true;
			}
		}
	}

	/**
	 * Builds (reads) the state cache from the specified input stream.
	 * 
	 * @param stateFile
	 * @throws Exception
	 * @throws CoreException
	 */
	private void readStateCache(File stateFile) throws Exception {
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_STATECACHEFACTORY,
					"parsing: " + stateFile.getCanonicalPath()); //$NON-NLS-1$
		}
		DefaultHandler stateCacheLoader = new DefaultHandler() {

			/** indicates if parsing is enabled */
			boolean isCorrectVersion = false;

			/** the workspace root (for quick access) */
			final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace()
					.getRoot();

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
			 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
			 */
			@Override
			public void startElement(String uri, String localName,
					String qName, Attributes attributes) throws SAXException {

				if (TAG_STATES.equals(qName)) {

					/* root node */

					// check version
					if (STATE_CACHE_VERSION.equals(attributes
							.getValue(ATTR_VERSION))) {
						isCorrectVersion = true;
					}

				} else if (isCorrectVersion && TAG_RESOURCE.equals(qName)) {

					/* resource node */

					// process resources
					String resourcePath = attributes.getValue(ATTR_PATH);
					if (null != resourcePath) {

						// determine resource
						IPath path = new Path(resourcePath);
						IResource resource = ROOT.findMember(path);
						if (resource != null && resource.isAccessible()) {
							try {
								// create cache
								StateCache cache = new StateCache(resource);
								cache.flags = Integer.parseInt(attributes
										.getValue(ATTR_STATE));
								cache.version = attributes
										.getValue(ATTR_VERSION);
								cache.symbolicLinkTarget = attributes
										.getValue(ATTR_SYMLINK_TARGET);
								cache.updateTimeStamp = Long
										.parseLong(attributes
												.getValue(ATTR_TIME_STAMP));

								// store cache
								synchronized (cacheMap) {
									cacheMap.put(resource, cache);
								}

								// make sure we know the view name for all
								// cached elements
								ClearCaseProvider.getViewName(resource);

								if (ClearCasePlugin.DEBUG_STATE_CACHE) {
									ClearCasePlugin.trace(
											TRACE_STATECACHEFACTORY,
											"state cache restored: " + cache); //$NON-NLS-1$
								}

								// fire change
								fireStateChanged(resource);
							} catch (RuntimeException e) {
								// ignore
							}

						}
					}
				}
			}
		};

		// parse
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(stateFile, stateCacheLoader);
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_STATECACHEFACTORY,
					"done: " + stateFile.getCanonicalPath()); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
	 * .eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		try {
			IResourceDelta rootDelta = event.getDelta();
			if (null != rootDelta) {
				IResourceDelta[] projectDeltas = rootDelta
						.getAffectedChildren();

				// determine resources to refresh
				final List<IResource> toRefresh = new ArrayList<IResource>();

				for (int i = 0; i < projectDeltas.length; i++) {
					IResourceDelta projectDelta = projectDeltas[i];

					// filter only shared projects
					if (RepositoryProvider.isShared((IProject) projectDelta
							.getResource())) {
						if (!isAffectedBy(rootDelta)) {
							continue;
						}

						if (projectDelta.getKind() == IResourceDelta.CHANGED
								&& (projectDelta.getFlags() & IResourceDelta.OPEN) != 0) {
							continue;
						}
						projectDelta.accept(new IResourceDeltaVisitor() {

							public boolean visit(IResourceDelta delta)
									throws CoreException {
								switch (delta.getKind()) {
								case IResourceDelta.REMOVED:
									// only remove cache
									removeSingle(delta.getResource());
									break;

								default:
									if (needsRefresh(delta)) {
										// refresh cache
										toRefresh.add(delta.getResource());
									}
								}

								return true;
							}
						});
					}
				}

				if (!toRefresh.isEmpty()) {
					refreshStateAsync(toRefresh.toArray(new IResource[toRefresh
							.size()]));
				}
			}
		} catch (CoreException e) {
			ClearCasePlugin.log(IStatus.ERROR,
					"Unable to do a update of resource", e); //$NON-NLS-1$
		}
	}

	/**
	 * Refreshes the state of the specified resources.
	 * 
	 * @param resources
	 *            The resources to refresh.
	 */
	void refreshStateAsync(IResource[] resources) {
		refreshState(resources, StateCacheJob.PRIORITY_DEFAULT);
	}

	/**
	 * Refreshes the state of the specified resources.
	 * 
	 * The refresh will take place using high priority.
	 * 
	 * @param resources
	 *            to refresh.
	 */
	void refreshStateAsyncHighPriority(IResource[] resources,
			IProgressMonitor monitor) {
		refreshState(resources, StateCacheJob.PRIORITY_HIGH);
	}

	/**
	 * Refreshes the state of the specified resources.
	 * 
	 * @param resources
	 */
	void refreshState(IResource[] resources, int priority) {
		StateCacheJob[] jobs = new StateCacheJob[resources.length];
		for (int i = 0; i < resources.length; i++) {
			StateCache cache = StateCacheFactory.getInstance().getWithNoUpdate(
					resources[i]);
			jobs[i] = new StateCacheJob(cache);
		}
		getJobQueue().schedule(jobs);
	}

	/**
	 * Indicates if the resource delta is really interesting for a refresh.
	 * 
	 * @param delta
	 * @return
	 */
	static boolean needsRefresh(IResourceDelta delta) {
		IResource resource = delta.getResource();

		// ignore linked folders
		if (resource.isLinked())
			return false;

		// check the global ignores from Team (includes derived resources)
		if (Team.isIgnoredHint(resource))
			return false;

		int interestingChangeFlags = IResourceDelta.CONTENT
				| IResourceDelta.SYNC | IResourceDelta.REPLACED
				| IResourceDelta.OPEN | IResourceDelta.TYPE;

		final int kind = delta.getKind();
		final int flags = delta.getFlags();
		if (kind == IResourceDelta.ADDED
				|| (kind == IResourceDelta.CHANGED && 0 != (flags & interestingChangeFlags)))
			// Returning true causes a state update whenever
			// the file is edited and saved inside Eclipse, too. That's not
			// nice, but acceptable
			return true;

		// System.out.println("ignored: " + delta);
		return false;
	}

	/**
	 * Returns whether a given delta contains some information relevant to the
	 * resource state, in particular it will not consider MARKER only deltas.
	 */
	static boolean isAffectedBy(IResourceDelta rootDelta) {
		// if (rootDelta == null) System.out.println("NULL DELTA");
		// long start = System.currentTimeMillis();
		if (rootDelta != null) {
			// use local exception to quickly escape from delta traversal
			class FoundRelevantDeltaException extends RuntimeException {

				private static final long serialVersionUID = 7668919997423339239L;
			}
			try {
				rootDelta.accept(new IResourceDeltaVisitor() {

					public boolean visit(IResourceDelta delta)
							throws CoreException {
						switch (delta.getKind()) {
						case IResourceDelta.ADDED:
						case IResourceDelta.REMOVED:
							throw new FoundRelevantDeltaException();
						case IResourceDelta.CHANGED:
							// if any flag is set but MARKER, this delta should
							// be considered
							if (delta.getAffectedChildren().length == 0 // only
									// check
									// leaf
									// delta
									// nodes
									&& (delta.getFlags() & ~IResourceDelta.MARKERS) != 0)
								throw new FoundRelevantDeltaException();
						}
						return true;
					}
				});
			} catch (FoundRelevantDeltaException e) {
				// System.out.println("RELEVANT DELTA detected in: "+
				// (System.currentTimeMillis() - start));
				return true;
			} catch (CoreException e) { // ignore delta if not able to traverse
			}
		}
		// System.out.println("IGNORE MARKER DELTA took: "+
		// (System.currentTimeMillis() - start));
		return false;
	}

	private final StateCacheJobQueue jobQueue = new StateCacheJobQueue();

	/**
	 * Returns the job queue.
	 * 
	 * @return the job queue
	 */
	StateCacheJobQueue getJobQueue() {
		return jobQueue;
	}
	

	/**
	 * Determine if there are any resources in the queue for async CC state refresh
	 * 
	 * @return true if there is at least one element in update queue
	 */
	public boolean hasPendingUpdates() {
		return !jobQueue.isEmpty();
	}

	/**
	 * Ensures the state cache for the specified resource is initialized.
	 * 
	 * @param resource
	 */
	void ensureInitialized(IResource resource) {
		StateCache cache = getWithNoUpdate(resource);
		if (cache.isUninitialized()) {
			cache.doUpdate();
		}
	}

	/**
	 * Indicates if the state cache factory is completly initialized.
	 * 
	 * @return <code>true</code> if initialized, <code>false</code> otherwise
	 */
	public boolean isInitialized() {
		return isStateCacheLoaded;
	}

	/**
	 * Cancels all pending state refreshes.
	 */
	public void cancelPendingRefreshes() {
		StateCacheFactory.getInstance().getJobQueue().cancel(true);
	}

	/**
	 * Interrupts all pending state refreshes.
	 */
	public void interruptPendingRefreshes() {
		StateCacheFactory.getInstance().getJobQueue().interrupt();
	}

	/**
	 * Cancels all pending state refreshes.
	 */
	public void resumePendingRefreshes() {
		StateCacheFactory.getInstance().getJobQueue().scheduleQueueRun();
	}

}