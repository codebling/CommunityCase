/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     IBM Corporation - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - new features, enhancements and bug fixes
 *******************************************************************************/
package net.sourceforge.eclipseccase;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.ClearCaseException;
import net.sourceforge.clearcase.ClearCaseInterface;
import net.sourceforge.clearcase.events.OperationListener;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PlatformUI;

/**
 * The ClearCase repository provider. Layer to clearcase java api.
 */
public class ClearCaseProvider extends RepositoryProvider {

	/** trace id */
	private static final String TRACE_ID_IS_IGNORED = "ClearCaseProvider#isIgnored"; //$NON-NLS-1$

	private static Map<String, String> viewLookupTable = new Hashtable<String, String>(
			200);

	private static Map<String, IContainer> viewAccessLookupTable = new Hashtable<String, IContainer>(
			30);

	private static Map<String, Boolean> snapshotViewLookupTable = new Hashtable<String, Boolean>(
			30);

	UncheckOutOperation UNCHECK_OUT = new UncheckOutOperation();

	CheckInOperation CHECK_IN = new CheckInOperation();

	CheckOutOperation CHECKOUT = new CheckOutOperation();

	UnHijackOperation UNHIJACK = new UnHijackOperation();

	AddOperation ADD = new AddOperation();

	RefreshStateOperation REFRESH_STATE = new RefreshStateOperation();

	CheckoutUnreservedOperation CO_UNRESERVED = new CheckoutUnreservedOperation();

	CheckoutReservedOperation CO_RESERVED = new CheckoutReservedOperation();

	private IMoveDeleteHook moveHandler = new MoveHandler(this);

	private String comment = ""; //$NON-NLS-1$

	public static final String ID = "net.sourceforge.eclipseccase.ClearcaseProvider"; //$NON-NLS-1$

	public static final Status OK_STATUS = new Status(IStatus.OK, ID,
			TeamException.OK, "OK", null); //$NON-NLS-1$
	public static final Status FAILED_STATUS = new Status(IStatus.ERROR, ID,
			TeamException.UNABLE, "FAILED", null); //$NON-NLS-1$

	public static final IStatus CANCEL_STATUS = Status.CANCEL_STATUS;

	boolean refreshResources = true;

	private OperationListener opListener = null;

	public ClearCaseProvider() {
		super();
	}

	UpdateOperation UPDATE = new UpdateOperation();

	DeleteOperation DELETE = new DeleteOperation();

	/**
	 * Checks if the monitor has been canceled.
	 * 
	 * @param monitor
	 */
	protected static void checkCanceled(IProgressMonitor monitor) {
		if (null != monitor && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.RepositoryProvider#configureProject()
	 */
	@Override
	public void configureProject() throws CoreException {
		// configureProject
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.RepositoryProvider#getID()
	 */
	@Override
	public String getID() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// deconfigure
	}

	public static ClearCaseProvider getClearCaseProvider(IResource resource) {
		if (null == resource)
			return null;
		IProject project = resource.getProject();
		if (null == project)
			return null;
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if (provider instanceof ClearCaseProvider) {
			// FIXME Achim: Whats this next line for?
			((ClearCaseProvider) provider).opListener = null;
			return (ClearCaseProvider) provider;
		} else
			return null;
	}

	/*
	 * @see SimpleAccessOperations#get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, int depth, IProgressMonitor progress)
			throws TeamException {
		execute(UPDATE, resources, depth, progress);
	}

	/*
	 * @see SimpleAccessOperations#checkout(IResource[], int, IProgressMonitor)
	 */
	public void checkout(IResource[] resources, int depth,
			IProgressMonitor progress) throws TeamException {
		try {
			execute(CHECKOUT, resources, depth, progress);
		} finally {
			setComment("");
		}
	}

	public void unhijack(IResource[] resources, int depth,
			IProgressMonitor progress) throws TeamException {
		try {
			execute(UNHIJACK, resources, depth, progress);
		} finally {
			setComment("");
		}
	}

	/**
	 * Invalidates the state cache of all specified resources.
	 * 
	 * @param resourceToRefresh
	 * @param monitor
	 * @throws CoreException
	 */
	public void refreshRecursive(IResource resourceToRefresh,
			IProgressMonitor monitor) throws CoreException {

		try {
			monitor.beginTask("Refreshing " + resourceToRefresh.getName(), 50);
			final List<IResource> toRefresh = new ArrayList<IResource>(80);
			monitor.subTask("collecting members");
			resourceToRefresh.accept(new IResourceVisitor() {

				public boolean visit(IResource resource) throws CoreException {
					if (!Team.isIgnoredHint(resource)) {
						toRefresh.add(resource);
					}
					return true;
				}
			});
			monitor.worked(30);
			monitor.subTask("scheduling updates");
			if (!toRefresh.isEmpty()) {
				StateCacheFactory.getInstance().refreshStateAsyncHighPriority(
						toRefresh.toArray(new IResource[toRefresh.size()]),
						monitor);
			}
			monitor.worked(10);
		} finally {
			monitor.done();
		}
	}

	public void refreshRecursive(IResource[] resources, IProgressMonitor monitor) {
		StateCacheFactory.getInstance().refreshStateAsyncHighPriority(
				resources, monitor);
	}

	/**
	 * Invalidates the state of the specified resource and only of the specified
	 * resource, not recursive
	 * 
	 * @param resource
	 */
	public void refresh(IResource resource) {
		StateCacheFactory.getInstance().get(resource).updateAsync(true);
	}

	/*
	 * @see SimpleAccessOperations#checkin(IResource[], int, IProgressMonitor)
	 */
	public void checkin(IResource[] resources, int depth,
			IProgressMonitor progressMonitor) throws TeamException {
		try {
			execute(CHECK_IN, resources, depth, progressMonitor);
		} finally {
			setComment("");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.RepositoryProvider#getRuleFactory()
	 */
	@Override
	public IResourceRuleFactory getRuleFactory() {
		return new ClearCaseResourceRuleFactory();
	}

	/**
	 * @see SimpleAccessOperations#uncheckout(IResource[], int,
	 *      IProgressMonitor)
	 */
	public void uncheckout(IResource[] resources, int depth,
			IProgressMonitor progress) throws TeamException {
		execute(UNCHECK_OUT, resources, depth, progress);
	}

	/**
	 * @see SimpleAccessOperations#delete(IResource[], IProgressMonitor)
	 */
	public void delete(IResource[] resources, IProgressMonitor progress)
			throws TeamException {
		try {
			execute(DELETE, resources, IResource.DEPTH_INFINITE, progress);
		} finally {
			setComment("");
		}
	}

	public void add(IResource[] resources, int depth, IProgressMonitor progress)
			throws TeamException {
		try {
			execute(ADD, resources, depth, progress);
		} finally {
			setComment("");
		}
	}

	public void unreserved(IResource[] resources, int depth,
			IProgressMonitor progress) throws TeamException {
		try {
			execute(CO_UNRESERVED, resources, depth, progress);
		} finally {
			setComment("");
		}
	}
	
	public void reserved(IResource[] resources, int depth,
			IProgressMonitor progress) throws TeamException {
		try {
			execute(CO_RESERVED, resources, depth, progress);
		} finally {
			setComment("");
		}
	}
	

	/*
	 * @see SimpleAccessOperations#moved(IPath, IResource, IProgressMonitor)
	 */
	public void moved(IPath source, IResource target, IProgressMonitor progress)
			throws TeamException {
		// moved
	}

	/**
	 * @see SimpleAccessOperations#isCheckedOut(IResource)
	 */
	public boolean isCheckedOut(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isCheckedOut();
	}

	/**
	 * Indicates if the specified resource is contained in a Snapshot view.
	 * 
	 * @param resource
	 * @return
	 */
	public boolean isSnapShot(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isSnapShot();
	}

	public boolean isHijacked(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isHijacked();
	}

	public boolean isUnknownState(IResource resource) {
		return StateCacheFactory.getInstance().isUninitialized(resource);
	}

	/**
	 * @see SimpleAccessOperations#isClearCaseElement(IResource)
	 */
	public boolean isClearCaseElement(IResource resource) {
		return StateCacheFactory.getInstance().get(resource)
				.isClearCaseElement();
	}

	/*
	 * @see SimpleAccessOperations#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isDirty();
	}

	public String getVersion(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).getVersion();
	}

	public String getPredecessorVersion(IResource resource) {
		return StateCacheFactory.getInstance().get(resource)
				.getPredecessorVersion();
	}

	public void showVersionTree(String element, File workingDir) {
		ClearCasePlugin.getEngine().showVersionTree(element, workingDir);

	}

	public void showFindMerge(File workingDir) {
		ClearCasePlugin.getEngine().showFindMerge(workingDir);

	}

	public String[] loadBrancheList(File workingDir) {
		return ClearCasePlugin.getEngine().loadBrancheList(workingDir);
	}

	public String[] searchFilesInBranch(String branchName, File workingDir,
			OperationListener listener) {
		return ClearCasePlugin.getEngine().searchFilesInBranch(branchName,
				workingDir, listener);
	}

	public void update(String element, int flags, boolean workingDir) {
		ClearCasePlugin.getEngine().update(element, flags, workingDir);
	}

	public void compareWithPredecessor(String element) {
		ClearCasePlugin.getEngine().compareWithPredecessor(element);

	}

	public void describeVersionGUI(String element) {
		ClearCasePlugin.getEngine().describeVersionGUI(element);

	}

	public void compareWithVersion(String element1, String element2) {
		ClearCasePlugin.getEngine().compareWithVersion(element1, element2);
	}

	public static String getViewName(IResource resource) {
		if (resource == null || resource.getProject() == null)
			return "";
		// assume that a complete project is inside one view
		String path;
		try {
			path = resource.getProject().getLocation().toOSString();
		} catch (NullPointerException e) {
			return "";
		}
		String res = viewLookupTable.get(path);
		if (res == null || res.length() == 0) {
			// use the originally given resource for the cleartool query
			if (!(resource instanceof IContainer)) {
				resource = resource.getParent();
			}
			res = getViewName(resource.getLocation().toOSString());
			if (res.length() > 0) {
				viewLookupTable.put(path, res);
				viewAccessLookupTable.put(res, (IContainer) resource);
			}
		}
		return res;
	}

	public static IContainer getViewFolder(final String viewname) {
		IContainer res = viewAccessLookupTable.get(viewname);
		if (res == null) {
			// TODO: search for a directory in view
		} else if (!res.isAccessible()) {
			// TODO: search for a new directory in view
		}
		return res;
	}

	public static String getViewName(final String path) {
		String res = viewLookupTable.get(path);
		if (res == null) {
			res = ClearCasePlugin.getEngine().getViewName(path);
			viewLookupTable.put(path, res);
		}
		return res;
	}

	public static String[] getUsedViewNames() {
		Set<String> views = new HashSet<String>();
		for (String v : viewLookupTable.values()) {
			views.add(v);
		}
		return views.toArray(new String[views.size()]);
	}

	/**
	 * Returns the view type of the view containing the resource.
	 * 
	 * @param resource
	 *            The resource inside a view.
	 * @return "dynamic" or "snapshot"
	 */
	public static String getViewType(IResource resource) {
		return isSnapshotView(getViewName(resource)) ? ClearCaseInterface.VIEW_TYPE_SNAPSHOT
				: ClearCaseInterface.VIEW_TYPE_DYNAMIC;
	}

	public static boolean isSnapshotView(final String viewName) {
		Boolean res = snapshotViewLookupTable.get(viewName);
		if (res == null) {
			if (viewName.length() == 0) {
				// special case, can happen after queries in non-view
				// directories
				res = false;
			} else {
				// standard case, we have a viewname, ask CC for the type
				String viewtype = ClearCasePlugin.getEngine().getViewType(
						viewName);
				res = viewtype.equals(ClearCaseInterface.VIEW_TYPE_SNAPSHOT);
			}
			snapshotViewLookupTable.put(viewName, res);
		}
		return res;
	}

	/**
	 * Returns the root of the view. An empty view root indicates a dynamic
	 * view.
	 * 
	 * @param resource
	 * @return
	 */
	public String getViewRoot(IResource resource) throws TeamException {
		return ClearCasePlugin.getEngine().getViewLocation(
				getViewName(resource));
	}

	/**
	 * Returns the name of the vob that contains the specified element
	 * 
	 * @param resource
	 * @return
	 */
	public String getVobName(IResource resource) throws TeamException {
		String viewRoot = getViewRoot(resource);
		IPath viewLocation = new Path(viewRoot);
		IPath resourceLocation = resource.getLocation();
		// ignore device when dealing with dynamic views
		if (viewRoot.length() == 0) {
			viewLocation = viewLocation.setDevice(resourceLocation.getDevice());
		}
		if (viewLocation.isPrefixOf(resourceLocation)) {
			IPath vobLocation = resourceLocation
					.removeFirstSegments(viewLocation.segmentCount());
			if (!ClearCasePlugin.isWindows() && vobLocation.segmentCount() > 0) {
				// on unix vobs are prefixed with directory named "/vobs"
				vobLocation = vobLocation.removeFirstSegments(1);
			}
			if (vobLocation.segmentCount() > 0)
				return vobLocation.segment(0);
		}
		return "none";
	}

	/**
	 * Returns the vob relative path of the specified element
	 * 
	 * @param resource
	 * @return the vob relativ path (maybe <code>null</code> if outside vob)
	 */
	public String getVobRelativPath(IResource resource) throws TeamException {
		String viewRoot = getViewRoot(resource);
		IPath viewLocation = new Path(viewRoot).setDevice(null); // ignore
		// device
		IPath resourceLocation = resource.getLocation().setDevice(null); // ignore
		// devices
		if (viewLocation.isPrefixOf(resourceLocation)) {
			IPath vobLocation = resourceLocation
					.removeFirstSegments(viewLocation.segmentCount());
			if (!ClearCasePlugin.isWindows() && vobLocation.segmentCount() > 0) {
				// on unix vobs are prefixed with directory named "/vobs"
				vobLocation = vobLocation.removeFirstSegments(1);
			}
			if (vobLocation.segmentCount() > 0)
				return vobLocation.removeFirstSegments(1).makeRelative()
						.toString();
		}
		return null;
	}

	/**
	 * Before the move operation we check if parent directories are checked out.
	 * We use that after the move has been performed in clearcase to set
	 * directories state (co/ci) as prior to move operation. If checkout is need
	 * then it is performed within the java clearcase package. The checkin is
	 * however performed within this method since we know the state prior to
	 * move operation and there is no need to send this information to the
	 * clearcase package. So an evetual checkin will be performed in this
	 * method.
	 * 
	 * @param source
	 * @param destination
	 * @param monitor
	 * @return result status of the operation.
	 */
	public IStatus move(IResource source, IResource destination,
			IProgressMonitor monitor) {
		try {
			monitor.beginTask("Moving " + source.getFullPath() + " to "
					+ destination.getFullPath(), 100);
			// Sanity check - can't move something that is not part of clearcase
			if (!isClearCaseElement(source))
				return new Status(
						IStatus.ERROR,
						ID,
						TeamException.NO_REMOTE_RESOURCE,
						MessageFormat
								.format(
										"Resource \"{0}\" is not under source control!",
										new Object[] { source.getFullPath()
												.toString() }), null);

			IStatus result = OK_STATUS;

			// boolean sourceParentCoBeforeOp =
			// isCheckedOut(source.getParent());
			// boolean targetParentCoBoforeOp = isCheckedOut(destination
			// .getParent());

			ClearCaseElementState[] state = null;

			if (ClearCasePlugin.isAutoCheckinParentAfterMoveAllowed()) {
				state = ClearCasePlugin.getEngine()
						.move(
								source.getLocation().toOSString(),
								destination.getLocation().toOSString(),
								getComment(),
								ClearCase.FORCE | ClearCase.CHECKIN
										| getCheckoutType(), opListener);
			} else {
				state = ClearCasePlugin.getEngine().move(
						source.getLocation().toOSString(),
						destination.getLocation().toOSString(), getComment(),
						ClearCase.FORCE | getCheckoutType(), opListener);

			}

			StateCacheFactory.getInstance().remove(source);
			updateState(source.getParent(), IResource.DEPTH_ZERO,
					new SubProgressMonitor(monitor, 10));
			updateState(destination.getParent(), IResource.DEPTH_ZERO,
					new SubProgressMonitor(monitor, 10));
			updateState(destination, IResource.DEPTH_INFINITE,
					new SubProgressMonitor(monitor, 10));

			if (!state[0].isMoved())
				return new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Could not move element: "
						// + ccStatus.message
						, null);
			return result;
		} finally {
			setComment("");
		}
	}

	public IStatus checkoutParent(IResource resource, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Checking out "
					+ resource.getParent().getFullPath().toString(), 10);
			IStatus result = OK_STATUS;
			String parent = null;
			// IProject's parent is the workspace directory, we want the
			// filesystem
			// parent if the workspace is not itself in clearcase
			boolean flag = resource instanceof IProject
					&& !isClearCaseElement(resource.getParent());
			if (flag) {
				parent = resource.getLocation().toFile().getParent().toString();
			} else {
				parent = resource.getParent().getLocation().toOSString();
			}
			monitor.worked(2);
			ClearCaseElementState elementState = ClearCasePlugin.getEngine()
					.getElementState(parent);
			if (!elementState.isElement()) {
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Could not find a parent that is a clearcase element",
						null);
				return result;
			}
			monitor.worked(2);
			if (!elementState.isCheckedOut() && !elementState.isLink()) {
				String[] element = { parent };
				ClearCaseElementState[] elementState2 = ClearCasePlugin
						.getEngine().checkout(element, getComment(),
								getCheckoutType(), opListener);

				monitor.worked(4);
				if (!flag) {
					updateState(resource.getParent(), IResource.DEPTH_ZERO,
							new SubProgressMonitor(monitor, 10));
				}
				if (elementState2 == null) {
					// TODO: Handle ccStatus.message.
					result = new Status(IStatus.ERROR, ID,
							TeamException.UNABLE,
							"Could not check out parent: " + "ccStatus", null);
				}
			}
			return result;
		} finally {
			monitor.done();
		}
	}

	// Notifies decorator that state has changed for an element
	public void updateState(IResource resource, int depth,
			IProgressMonitor monitor) {
		try {
			monitor.beginTask("Refreshing " + resource.getFullPath(), 20);
			if (!refreshResources) {
				StateCacheFactory.getInstance().removeSingle(resource);
				monitor.worked(10);
			} else {
				resource.refreshLocal(depth,
						new SubProgressMonitor(monitor, 10));
			}

			if (resource.exists()) {
				doUpdateState(resource, depth, new SubProgressMonitor(monitor,
						10));
			} else {
				StateCacheFactory.getInstance().refreshStateAsyncHighPriority(
						new IResource[] { resource }, null);
			}
		} catch (CoreException ex) {
			ClearCasePlugin.log(IStatus.ERROR,
					"Error refreshing ClearCase state: " + ex.getMessage(), ex);
		} finally {
			monitor.done();
		}
	}

	private IStatus doUpdateState(IResource resource, int depth,
			IProgressMonitor progressMonitor) {
		IStatus result = execute(REFRESH_STATE, resource, depth,
				progressMonitor);
		return result;
	}

	/**
	 * @see RepositoryProvider#getMoveDeleteHook()
	 */
	@Override
	public IMoveDeleteHook getMoveDeleteHook() {
		return moveHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.team.core.RepositoryProvider#getFileModificationValidator2()
	 */
	@Override
	public FileModificationValidator getFileModificationValidator2() {
		return ClearCasePlugin.getInstance().getClearCaseModificationHandler();
	}

	/**
	 * Gets the comment.
	 * 
	 * @return Returns a String
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment.
	 * 
	 * @param comment
	 *            The comment to set
	 */
	public void setComment(String comment) {
		// escape comment if enabled
		// if (comment.trim().length() > 0 && ClearCasePlugin.isCommentEscape())
		// comment = ClearCaseUtil.getEscaped(comment);
		this.comment = comment;
	}

	// Out of sheer laziness, I appropriated the following code from the team
	// provider example =)
	private final class RefreshStateOperation implements IRecursiveOperation {

		public IStatus visit(IResource resource, IProgressMonitor monitor) {
			try {
				checkCanceled(monitor);

				monitor.beginTask("Refreshing State " + resource.getFullPath(),
						10);
				// probably overkill/expensive to do it here - should do it
				// on a
				// case by case basis for each method that actually changes
				// state
				StateCache cache = StateCacheFactory.getInstance()
						.get(resource);
				if (!cache.isSymbolicLink()) {
					// force update immediately. For symlinks, the symlink
					// target has to be updated first, see below
					cache.doUpdate();
				}
				// check if a symbolic link target is also in our workspace
				if (cache.isSymbolicLink()
						&& null != cache.getSymbolicLinkTarget()) {
					File target = new File(cache.getSymbolicLinkTarget());
					if (!target.isAbsolute()) {
						target = null != cache.getPath() ? new File(cache
								.getPath()).getParentFile() : null;
						if (null != target) {
							target = new File(target, cache
									.getSymbolicLinkTarget());
						}
					}
					if (null != target && target.exists()) {
						IPath targetLocation = new Path(target
								.getAbsolutePath());
						IResource[] resources = null;
						if (target.isDirectory()) {
							resources = ResourcesPlugin.getWorkspace()
									.getRoot().findContainersForLocation(
											targetLocation);
						} else {
							resources = ResourcesPlugin.getWorkspace()
									.getRoot().findFilesForLocation(
											targetLocation);
						}
						if (null != resources) {
							for (int i = 0; i < resources.length; i++) {
								IResource foundResource = resources[i];
								ClearCaseProvider provider = ClearCaseProvider
										.getClearCaseProvider(foundResource);
								if (null != provider) {
									StateCacheFactory.getInstance().get(
											foundResource).updateAsync(false);
									// after the target is updated, we must
									// update the
									// symlink itself again :-(
									cache.updateAsync(false);
								}
							}
						}
					}
				}
				return OK_STATUS;
			} finally {
				monitor.done();
			}
		}
	}

	private final class AddOperation implements IRecursiveOperation {

		ArrayList<IResource> privateElement = new ArrayList<IResource>();

		public IStatus visit(IResource resource, IProgressMonitor monitor) {
			try {
				monitor.beginTask(
						"Adding " + resource.getFullPath().toString(), 100);
				IStatus result = OK_STATUS;
				// Sanity check - can't add something that already is under VC
				if (isClearCaseElement(resource))
					// return status with severity OK
					return new Status(
							IStatus.OK,
							ID,
							TeamException.UNABLE,
							MessageFormat
									.format(
											"Resource \"{0}\" is already under source control!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				result = findPrivateElements(resource, monitor);

				if (result.isOK()) {
					Collections.reverse(privateElement);
					for (Object element : privateElement) {
						IResource myResource = (IResource) element;
						if (myResource.getType() == IResource.FOLDER) {
							result = makeFolderElement(myResource, monitor);
						} else if (myResource.getType() == IResource.FILE) {
							result = makeFileElement(myResource, monitor);
						}

					}
				}

				// Add check recursive checkin of files.
				if (ClearCasePlugin.isAddWithCheckin() && result == OK_STATUS) {
					for (Object element : privateElement) {
						IResource res = (IResource) element;
						if (isCheckedOut(res)) {
							ClearCasePlugin.getEngine().checkin(
									new String[] { res.getLocation()
											.toOSString() }, getComment(),
									ClearCase.NONE, opListener);

						}

					}
				}

				monitor.worked(40);
				return result;
			} finally {
				monitor.done();
				privateElement.clear();
			}
		}

		/**
		 * Recursively from bottom of file path to top until clearcase element
		 * is found.
		 * 
		 * @param resource
		 * @param monitor
		 * @return
		 */
		private IStatus findPrivateElements(IResource resource,
				IProgressMonitor monitor) {
			IStatus result = OK_STATUS;
			IResource parent = resource.getParent();

			// When resource is a project, try checkout its parent, and if
			// that fails,
			// then neither project nor workspace is in clearcase.
			if (isClearCaseElement(parent)) {
				privateElement.add(resource);
				updateState(parent, IResource.DEPTH_ZERO,
						new SubProgressMonitor(monitor, 10));// make sure state
				// for parent is
				// correct.
				if (!isCheckedOut(parent)) {
					ClearCaseElementState[] state = ClearCasePlugin.getEngine()
							.checkout(
									new String[] { parent.getLocation()
											.toOSString() }, getComment(),
									ClearCase.NONE, opListener);
					if (state[0].isCheckedOut()) {
						updateState(parent, IResource.DEPTH_ZERO,
								new SubProgressMonitor(monitor, 10));
					}

				}

			} else if (resource instanceof IProject
					&& !(isClearCaseElement(resource))) {
				// We reached project top and it is not a cc element.
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Add failed: " + "project folder " + resource.getName()
								+ " is not an element is not an cc element",
						null);
			} else {
				privateElement.add(resource);
				findPrivateElements(parent, new SubProgressMonitor(monitor, 10));
			}
			return result;
		}
	}

	private Status makeFileElement(IResource resource, IProgressMonitor monitor) {
		Status result = OK_STATUS;

		ClearCaseElementState state = ClearCasePlugin.getEngine().add(
				resource.getLocation().toOSString(), false, getComment(),
				ClearCase.PTIME | (ClearCasePlugin.isUseMasterForAdd()? ClearCase.MASTER:ClearCase.NONE), opListener);

		if (state.isElement()) {
			// Do nothing!
		} else {
			result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
					"Add failed: " + "Could not add element"
							+ resource.getName(), null);
		}
		try {
			resource.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(
					monitor, 10));
		} catch (CoreException e) {
			System.out.println("We got an exception!");
			e.printStackTrace();
			result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
					"Add failed: " + "Exception" + e.getMessage(), null);
		}
		updateState(resource, IResource.DEPTH_ZERO, new SubProgressMonitor(
				monitor, 10));
		if (result.isOK()) {
			result = forceSetChgrp(resource);

		}

		return result;
	}

	private Status makeFolderElement(IResource resource,
			IProgressMonitor monitor) {
		File dir = new File(resource.getLocation().toOSString());
		File tmpDir = new File(dir.getParentFile(), dir.getName() + ".tmp");

		Status result = OK_STATUS;
		try {
			// rename target dir to <name>.tmp since clearcase cannot make
			// an directory element out of an existing view private one.
			if (!dir.renameTo(tmpDir)) {
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Add failed: " + "Could not rename " + dir.getPath()
								+ " to " + tmpDir.getPath()
								+ resource.getName(), null);
			}

			// Now time to create the original directory in
			// clearcase.
			ClearCaseElementState state = ClearCasePlugin.getEngine().add(
					resource.getLocation().toOSString(), true, getComment(),
					ClearCasePlugin.isUseMasterForAdd()? ClearCase.MASTER:ClearCase.NONE, opListener);
			if (!state.isElement()) {
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Add failed: " + "Could not add element"
								+ resource.getName(), null);
			}
			// Now move back the content of <name>.tmp to cc created one.
			if (!moveDirRec(tmpDir, dir)) {
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Could not move back the content of " + dir.getPath()
								+ " as part of adding it to Clearcase:\n"
								+ "Its old content is in " + tmpDir.getName()
								+ ". Please move it back manually", null);

			}

			if (result.isOK()) {
				result = forceSetChgrp(resource);
			}

			// Now move back the content of tmp to original.
			// To avoid CoreException do a refreshLocal(). Does
			// not recognize the cc created resource directory.
			resource.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(
					monitor, 10));
			updateState(resource, IResource.DEPTH_ZERO, new SubProgressMonitor(
					monitor, 10));

		} catch (CoreException ce) {
			System.out.println("We got an exception!");
			ce.printStackTrace();
			result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
					"Add failed: " + "Exception" + ce.getMessage(), null);
		}

		return result;
	}

	private Status forceSetChgrp(IResource resource) {
		Status result = OK_STATUS;
		String group = ClearCasePlugin.getClearCasePrimaryGroup().trim();
		if (group.length() > 0) {
			try {
				ClearCasePlugin.getEngine().setGroup(
						resource.getLocation().toOSString(), group, opListener);
			} catch (Exception e) {
				result = new Status(IStatus.ERROR, ID, TeamException.UNABLE,
						"Chgrp failed: " + "Could not change group element "
								+ resource.getName() + "\n" + e.getMessage(),
						null);
			}

		}
		return result;
	}

	private final class UncheckOutOperation implements IRecursiveOperation {

		public IStatus visit(final IResource resource,
				final IProgressMonitor monitor) {
			try {
				monitor.beginTask("Uncheckout " + resource.getFullPath(), 100);
				StateCache cache = getCache(resource);
				final StateCache targetElement = getFinalTargetElement(cache);
				// Sanity check - can't process something that is not part of
				// clearcase
				if (targetElement == null
						|| !targetElement.isClearCaseElement())
					return new Status(
							IStatus.WARNING,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				// Sanity check - can't uncheckout something that is not checked
				// out
				if (!targetElement.isCheckedOut())
					// return severity OK
					return new Status(IStatus.OK, ID,
							TeamException.NOT_CHECKED_OUT,
							MessageFormat.format(
									"Resource \"{0}\" is not checked out!",
									new Object[] { targetElement.getPath() }),
							null);
				IStatus result = OK_STATUS;

				// Yes continue checking out.
				int flags = ClearCase.RECURSIVE;
				if (ClearCasePlugin.isKeepChangesAfterUncheckout()) {
					flags |= ClearCase.KEEP;
				}

				ClearCasePlugin.getEngine().uncheckout(
						new String[] { targetElement.getPath() }, flags,
						opListener);
				monitor.worked(40);
				updateState(resource, IResource.DEPTH_ZERO,
						new SubProgressMonitor(monitor, 10));

				// if (!status.status) {
				// result = new Status(IStatus.ERROR, ID,
				// TeamException.UNABLE, "Uncheckout failed: "
				// + status.message, null);
				// }
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class DeleteOperation implements IIterativeOperation {

		public IStatus visit(IResource resource, int depth,
				IProgressMonitor monitor) {
			try {
				monitor.beginTask("Deleting " + resource.getFullPath(), 100);
				// Sanity check - can't delete something that is not part of
				// clearcase
				if (!isClearCaseElement(resource))
					return new Status(
							IStatus.ERROR,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				IStatus result = checkoutParent(resource,
						new SubProgressMonitor(monitor, 10));
				if (result.isOK()) {
					ClearCasePlugin.getEngine()
							.delete(
									new String[] { resource.getLocation()
											.toOSString() }, getComment(),
									ClearCase.RECURSIVE | ClearCase.KEEP,
									opListener);
					monitor.worked(40);
					updateState(resource, IResource.DEPTH_INFINITE,
							new SubProgressMonitor(monitor, 10));
					// if (!status.status) {
					// result = new Status(IStatus.ERROR, ID,
					// TeamException.UNABLE, "Delete failed: "
					// + status.message, null);
					// }
				}
				return result;
			} finally {
				monitor.done();
			}
		}

	}

	protected final class CheckInOperation implements IRecursiveOperation {

		public IStatus visit(IResource resource, IProgressMonitor monitor) {
			try {
				monitor.beginTask("Checkin in " + resource.getFullPath(), 100);
				StateCache cache = getCache(resource);
				final StateCache targetElement = getFinalTargetElement(cache);
				// Sanity check - can't check in something that is not part of
				// clearcase
				if (targetElement == null
						|| !targetElement.isClearCaseElement())
					return new Status(
							IStatus.WARNING,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				// Sanity check - can't checkin something that is not checked
				// out
				if (!targetElement.isCheckedOut())
					// return status with severity OK
					return new Status(IStatus.OK, ID,
							TeamException.NOT_CHECKED_OUT,
							MessageFormat.format(
									"Resource \"{0}\" is not checked out!",
									new Object[] { targetElement.getPath() }),
							null);
				IStatus result = OK_STATUS;

				if (ClearCasePlugin.isCheckinIdenticalAllowed()) {
					ClearCasePlugin.getEngine().checkin(
							new String[] { targetElement.getPath() },
							getComment(),
							ClearCase.PTIME | ClearCase.IDENTICAL, opListener);
				} else {

					try {
						ClearCasePlugin.getEngine().checkin(
								new String[] { targetElement.getPath() },
								getComment(), ClearCase.PTIME, opListener);
					} catch (ClearCaseException cce) {
						// check error
						switch (cce.getErrorCode()) {
						case ClearCase.ERROR_PREDECESSOR_IS_IDENTICAL:
							result = new Status(
									IStatus.ERROR,
									ID,
									TeamException.NOT_CHECKED_IN,
									MessageFormat
											.format(
													Messages
															.getString("ClearCasePlugin.error.checkin.identicalPredecessor"),
													new Object[] { cce
															.getElements() }),
									null);
							break;
						case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
							result = new Status(
									IStatus.ERROR,
									ID,
									TeamException.NOT_CHECKED_IN,
									MessageFormat
											.format(
													Messages
															.getString("ClearCasePlugin.error.checkin.elementHasCheckouts"),
													new Object[] { cce
															.getElements() }),
									null);
							break;
						case ClearCase.ERROR_MOST_RECENT_NOT_PREDECESSOR_OF_THIS_VERSION:
							// Only support for: To merge the latest version
							// with your checkout
							// getVersion --> \branch\CHECKEDOUT.
							String branchName = getBranchName(getVersion(resource));
							String latestVersion = resource.getLocation()
									.toOSString()
									+ "@@" + branchName + "LATEST";
							// TODO: merge method void but state is set to
							// MERGED when ok. Need handling for this.
							ClearCasePlugin.getEngine().merge(
									targetElement.getPath(),
									new String[] { latestVersion },
									ClearCase.GRAPHICAL);
							// Now it should be ok to checkin.
							// TODO: Check if merge was successful then checkin.
							// if not clean up.
							ClearCasePlugin.getEngine().checkin(
									new String[] { targetElement.getPath() },
									getComment(), ClearCase.PTIME, opListener);

							break;

						default:
							result = new Status(
									IStatus.ERROR,
									ID,
									TeamException.NOT_CHECKED_IN,
									MessageFormat
											.format(
													Messages
															.getString("ClearCasePlugin.error.checkin.unknown"),
													new Object[] { cce
															.getElements() }),
									null);

							break;
						}

					}
				}

				monitor.worked(40);
				updateState(resource, IResource.DEPTH_ZERO,
						new SubProgressMonitor(monitor, 10));
				// if (!status.status) {
				// result = new Status(IStatus.ERROR, ID,
				// TeamException.UNABLE, "Checkin failed: "
				// + status.message, null);
				// }
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class CheckOutOperation implements IRecursiveOperation {

		public IStatus visit(final IResource resource,
				final IProgressMonitor monitor) {
			try {
				monitor
						.beginTask("Checking out " + resource.getFullPath(),
								100);
				StateCache cache = getCache(resource);
				final StateCache targetElement = getFinalTargetElement(cache);
				// Sanity check - can't checkout something that is not part of
				// clearcase
				if (targetElement == null
						|| !targetElement.isClearCaseElement())
					return new Status(
							IStatus.WARNING,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);

				// Sanity check - can't checkout something that is already
				// checked out
				if (targetElement.isCheckedOut())
					// return status with severity OK
					return new Status(IStatus.OK, ID,
							TeamException.NOT_CHECKED_IN, MessageFormat.format(
									"Resource \"{0}\" is already checked out!",
									new Object[] { targetElement.getPath() }),
							null);

				IStatus result = OK_STATUS;

				// update if necessary
				if (ClearCasePlugin.isCheckoutLatest()
						&& targetElement.isSnapShot()) {
					monitor.subTask("Updating " + targetElement.getPath());
					update(resource.getFullPath().toOSString(), 0, false);

				}
				monitor.worked(20);

				// only checkout if update was successful
				if (result == OK_STATUS) {
					monitor.subTask("Checking out " + targetElement.getPath());
					try {
						ClearCasePlugin
								.getEngine()
								.checkout(
										new String[] { targetElement.getPath() },
										getComment(),
										getCheckoutType()
												| ClearCase.PTIME
												| (targetElement.isHijacked() ? ClearCase.HIJACKED
														: ClearCase.NONE),
										opListener);
					} catch (ClearCaseException cce) {
						switch (cce.getErrorCode()) {
						case ClearCase.ERROR_ELEMENT_HAS_CHECKOUTS:
							// Ask if you wan't to check-out unreserved since we
							// only accepted
							// reserved checkouts.
							PlatformUI.getWorkbench().getDisplay().syncExec(
									new Runnable() {

										public void run() {
											Shell activeShell = PlatformUI
													.getWorkbench()
													.getDisplay()
													.getActiveShell();
											MessageDialog checkoutQuestion = new MessageDialog(
													activeShell,
													"Checkout",
													null,
													"Resource already checked-out reserved.\nDo you want to check-out unreserved?",
													MessageDialog.QUESTION,
													new String[] { "Yes", "No",
															"Cancel" }, 0);
											int returncode = checkoutQuestion
													.open();
											/* Yes=0 No=1 Cancel=2 */
											if (returncode == 0) {
												// Yes continue checking out but
												// unreserved.
												ClearCasePlugin
														.getEngine()
														.checkout(
																new String[] { targetElement
																		.getPath() },
																getComment(),
																ClearCase.UNRESERVED
																		| ClearCase.PTIME,
																opListener);
												monitor.worked(40);
												updateState(resource,
														IResource.DEPTH_ZERO,
														new SubProgressMonitor(
																monitor, 10));
											}
										}
									});
							break;

						default:
							result = new Status(
									IStatus.ERROR,
									ID,
									TeamException.UNABLE,
									MessageFormat
											.format(
													Messages
															.getString("ClearCasePlugin.error.checkin.unknown"),
													new Object[] { cce
															.getElements() }),
									null);

							break;
						}
					}

				}
				monitor.worked(20);

				// update state of target element first (if symlink)
				if (!targetElement.equals(cache)) {
					targetElement.doUpdate();
				}
				// update state
				updateState(resource, IResource.DEPTH_ZERO,
						new SubProgressMonitor(monitor, 10));
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class UnHijackOperation implements IRecursiveOperation {

		public IStatus visit(final IResource resource,
				final IProgressMonitor monitor) {
			try {
				monitor.beginTask("Checkin out " + resource.getFullPath(), 100);

				// Sanity check - can't checkout something that is not part of
				// clearcase
				if (!isHijacked(resource))
					return new Status(
							IStatus.WARNING,
							ID,
							TeamException.NOT_AUTHORIZED,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a Hijacked ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);

				IStatus result = OK_STATUS;

				try {
					/* remove existing xx.keep file */
					File keep = new File(resource.getLocation().toOSString()
							+ ".keep");
					if (keep.exists()) {
						keep.delete();
					}

					/* rename existing xx.keep file */
					keep = new File(resource.getLocation().toOSString());
					if (keep.exists()) {
						keep.renameTo(new File(resource.getLocation()
								.toOSString()
								+ ".keep"));
					}
				} catch (Exception e) {
					result = FAILED_STATUS;
				}
				monitor.worked(20);

				if (result == OK_STATUS) {
					// update if necessary
					if (ClearCasePlugin.isCheckoutLatest()
							&& isSnapShot(resource)) {
						monitor.subTask("Updating " + resource.getName());
						update(resource.getLocation().toOSString(), 0, false);

					}
				}
				monitor.worked(20);

				// update state
				updateState(resource.getParent(), IResource.DEPTH_ONE,
						new SubProgressMonitor(monitor, 10));

				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class UpdateOperation implements IIterativeOperation {

		public IStatus visit(IResource resource, int depth,
				IProgressMonitor monitor) {
			try {
				monitor.beginTask("Updating " + resource.getFullPath(), 100);

				// Sanity check - can't update something that is not part of
				// clearcase
				if (!isClearCaseElement(resource))
					return new Status(
							IStatus.ERROR,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				IStatus result = OK_STATUS;
				String element = resource.getLocation().toOSString();
				ClearCasePlugin.getEngine().update(element, 0, false);
				monitor.worked(40);
				updateState(resource, IResource.DEPTH_INFINITE,
						new SubProgressMonitor(monitor, 10));
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class CheckoutUnreservedOperation implements
			IIterativeOperation {

		public IStatus visit(IResource resource, int depth,
				IProgressMonitor monitor) {
			try {
				monitor.beginTask("Changing checkout to unreserved "
						+ resource.getFullPath(), 100);

				// Sanity check - can't update something that is not part of
				// clearcase
				if (!isClearCaseElement(resource))
					return new Status(
							IStatus.ERROR,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				IStatus result = OK_STATUS;
				String element = resource.getLocation().toOSString();
				ClearCasePlugin.getEngine().unreserved(
						new String[] { element }, null, 0, opListener);
				monitor.worked(40);
				updateState(resource, IResource.DEPTH_INFINITE,
						new SubProgressMonitor(monitor, 10));
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	private final class CheckoutReservedOperation implements
			IIterativeOperation {

		public IStatus visit(IResource resource, int depth,
				IProgressMonitor monitor) {
			try {
				monitor.beginTask("Changing checkout to reserved "
						+ resource.getFullPath(), 100);

				// Sanity check - can't update something that is not part of
				// clearcase
				if (!isClearCaseElement(resource))
					return new Status(
							IStatus.ERROR,
							ID,
							TeamException.NO_REMOTE_RESOURCE,
							MessageFormat
									.format(
											"Resource \"{0}\" is not a ClearCase element!",
											new Object[] { resource
													.getFullPath().toString() }),
							null);
				IStatus result = OK_STATUS;
				String element = resource.getLocation().toOSString();
				ClearCasePlugin.getEngine().reserved(
						new String[] { element }, null, 0, opListener);
				monitor.worked(40);
				updateState(resource, IResource.DEPTH_INFINITE,
						new SubProgressMonitor(monitor, 10));
				return result;
			} finally {
				monitor.done();
			}
		}
	}

	/**
	 * These interfaces are to operations that can be performed on the array of
	 * resources, and on all resources identified by the depth parameter.
	 * 
	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
	 */
	public static interface IOperation {
		// empty
	}

	public static interface IIterativeOperation extends IOperation {

		public IStatus visit(IResource resource, int depth,
				IProgressMonitor progress);
	}

	public static interface IRecursiveOperation extends IOperation {

		public IStatus visit(IResource resource, IProgressMonitor progress);
	}

	/**
	 * Perform the given operation on the array of resources, each to the
	 * specified depth. Throw an exception if a problem ocurs, otherwise remain
	 * silent.
	 */
	protected void execute(IOperation operation, IResource[] resources,
			int depth, IProgressMonitor progress) throws TeamException {
		if (null == progress) {
			progress = new NullProgressMonitor();
		}
		// Create an array to hold the status for each resource.
		MultiStatus multiStatus = new MultiStatus(getID(), TeamException.OK,
				"OK", null);
		// For each resource in the local resources array until we have errors.
		try {
			progress.beginTask("Processing", 1000 * resources.length);
			for (int i = 0; i < resources.length
					&& !multiStatus.matches(IStatus.ERROR); i++) {
				progress.subTask(resources[i].getFullPath().toString());
				if (!isIgnored(resources[i])) {
					if (operation instanceof IRecursiveOperation) {
						multiStatus.merge(execute(
								(IRecursiveOperation) operation, resources[i],
								depth, new SubProgressMonitor(progress, 1000)));
					} else {
						multiStatus
								.merge(((IIterativeOperation) operation).visit(
										resources[i], depth,
										new SubProgressMonitor(progress, 1000)));
					}
				} else {
					progress.worked(1000);
				}
			}
			// Finally, if any problems occurred, throw the exeption with all
			// the statuses,
			// but if there were no problems exit silently.
			if (!multiStatus.isOK()) {
				String message = multiStatus.matches(IStatus.ERROR) ? "There were errors that prevent the requested operation from finishing successfully."
						: "The requested operation finished with warnings.";
				throw new TeamException(new MultiStatus(
						multiStatus.getPlugin(), multiStatus.getCode(),
						multiStatus.getChildren(), message, multiStatus
								.getException()));
			}
			// Cause all the resource changes to be broadcast to listeners.
			// TeamPlugin.getManager().broadcastResourceStateChanges(resources);
		} finally {
			progress.done();
		}
	}

	/**
	 * Perform the given operation on a resource to the given depth.
	 */
	protected IStatus execute(IRecursiveOperation operation,
			IResource resource, int depth, IProgressMonitor progress) {
		if (null == progress) {
			progress = new NullProgressMonitor();
		}
		try {
			progress.beginTask("Processing", 1000);
			// Visit the given resource first.
			IStatus status = operation.visit(resource, new SubProgressMonitor(
					progress, 200));
			// If the resource is a file then the depth parameter is irrelevant.
			if (resource.getType() == IResource.FILE)
				return status;
			// If we are not considering any members of the container then we
			// are done.
			if (depth == IResource.DEPTH_ZERO)
				return status;
			// If the operation was unsuccessful, do not attempt to go deep.
			if (status.matches(IStatus.ERROR)) // if (!status.isOK())
				return status;

			// if operation was cancaled, do not go deep
			if (CANCEL_STATUS == status)
				return OK_STATUS;

			// If the container has no children then we are done.
			IResource[] members = getMembers(resource);
			if (members.length == 0)
				return status;
			// There are children and we are going deep, the response will be a
			// multi-status.
			MultiStatus multiStatus = new MultiStatus(status.getPlugin(),
					status.getCode(), status.getMessage(), status
							.getException());
			// The next level will be one less than the current level...
			int childDepth = (depth == IResource.DEPTH_ONE) ? IResource.DEPTH_ZERO
					: IResource.DEPTH_INFINITE;
			// Collect the responses in the multistatus (use merge to flatten
			// the tree).
			int ticks = 800 / members.length;
			for (int i = 0; i < members.length
					&& !multiStatus.matches(IStatus.ERROR); i++) {
				progress.subTask(members[i].getFullPath().toString());
				if (!isIgnored(members[i])) {
					multiStatus
							.merge(execute(operation, members[i], childDepth,
									new SubProgressMonitor(progress, ticks)));
				} else {
					progress.worked(ticks);
				}
			}
			// correct the MultiStatus message
			if (!multiStatus.isOK()) {
				/*
				 * Remember: the multi status was created with "OK" as message!
				 * This is not meaningful anymore. We have to correct it.
				 */
				String message = multiStatus.matches(IStatus.ERROR) ? "There were errors that prevent the requested operation from finishing successfully."
						: "The requested operation finished with warnings.";
				multiStatus = new MultiStatus(multiStatus.getPlugin(),
						multiStatus.getCode(), multiStatus.getChildren(),
						message, multiStatus.getException());
			}
			return multiStatus;
		} finally {
			progress.done();
		}
	}

	protected IResource[] getMembers(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			try {
				return ((IContainer) resource).members();
			} catch (CoreException exception) {
				exception.printStackTrace();
				throw new RuntimeException();
			}
		} // end-if
		else
			return new IResource[0];
	}

	/**
	 * @see org.eclipse.team.core.RepositoryProvider#canHandleLinkedResources()
	 */
	@Override
	public boolean canHandleLinkedResources() {
		return true;
	}

	@Override
	public boolean canHandleLinkedResourceURI() {
		return true;
	}

	/**
	 * Indicates if a resource is ignored and not handled.
	 * <p>
	 * Resources are never ignored, if they have a remote resource.
	 * </p>
	 * 
	 * @param resource
	 * @return
	 */
	public boolean isIgnored(IResource resource) {
		// // ignore eclipse linked resource
		// if (resource.isLinked()) {
		// if (ClearCasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES) {
		// ClearCasePlugin.trace(TRACE_ID_IS_IGNORED,
		//						"linked resource: " + resource); //$NON-NLS-1$
		// }
		// return true;
		// }

		// never ignore handled resources
		if (isClearCaseElement(resource))
			return false;

		// never ignore workspace root
		IResource parent = resource.getParent();
		if (null == parent)
			return false;

		// check the global ignores from Team (includes derived resources)
		if (Team.isIgnoredHint(resource)) {
			if (ClearCasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES) {
				ClearCasePlugin.trace(TRACE_ID_IS_IGNORED,
						"ignore hint from team plug-in: " + resource); //$NON-NLS-1$
			}
			return true;
		}

		// never ignore uninitialized resources
		if (isUnknownState(resource))
			return false;

		// ignore resources outside view
		if (!isInsideView(resource)) {
			if (ClearCasePlugin.DEBUG_PROVIDER_IGNORED_RESOURCES) {
				ClearCasePlugin.trace(TRACE_ID_IS_IGNORED,
						"outside view: " + resource); //$NON-NLS-1$
			}
			return true;
		}

		// bug 904248: do not ignore if parent is a linked resource
		if (parent.isLinked())
			return false;

		// check the parent, if the parent is ignored
		// then this resource is ignored also
		return isIgnored(parent);
	}

	/**
	 * @param resource
	 * @return
	 */
	public boolean isSymbolicLink(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isSymbolicLink();
	}

	/**
	 * @param resource
	 * @return
	 */
	public boolean isSymbolicLinkTargetValid(IResource resource) {
		return StateCacheFactory.getInstance().get(resource)
				.isSymbolicLinkTargetValid();
	}

	/**
	 * @param resource
	 * @return
	 */
	public String getSymbolicLinkTarget(IResource resource) {
		return StateCacheFactory.getInstance().get(resource)
				.getSymbolicLinkTarget();
	}

	/**
	 * Indicates if the specified resource is edited (checked out) by someone
	 * else.
	 * 
	 * @param childResource
	 * @return <code>true</code> if the specified resource is edited (checked
	 *         out) by someone else, <code>false</code> otherwise
	 */
	public boolean isEdited(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isEdited();
	}

	/**
	 * Indicates if the specified resource is a view root directory containing
	 * vobs.
	 * 
	 * @param resource
	 * @return <code>true</code> if the specified resource is a view root
	 *         directory
	 */
	public boolean isViewRoot(IResource resource) {
		/*
		 * todo: we need a better check for the view root; this only supports
		 * structures where a project is the view directory containing the vobs
		 */
		return null != resource && resource.getType() == IResource.PROJECT
				&& !isClearCaseElement(resource);
	}

	/**
	 * Indicates if the specified resource is a vob root directory.
	 * 
	 * @param resource
	 * @return <code>true</code> if the specified resource is a vob root
	 *         directory
	 */
	public boolean isVobRoot(IResource resource) {
		/*
		 * todo: we need a better check for the vob root; this only supports
		 * structures where a project is the view directory containing the vobs
		 */
		// return resource.getType() == IResource.FOLDER && !resource.isLinked()
		// && isViewRoot(resource.getParent());
		return false;
	}

	/**
	 * Indicates if the specified resource is inside a view directory.
	 * 
	 * @param resource
	 * @return <code>true</code> if the specified resource is a view directory
	 */
	public boolean isInsideView(IResource resource) {
		return StateCacheFactory.getInstance().get(resource).isInsideView();
	}

	/**
	 * Get the StateCache for an element
	 * 
	 * @param resource
	 * @return the corresponding StateCache
	 */
	public StateCache getCache(IResource resource) {
		return StateCacheFactory.getInstance().get(resource);
	}

	/**
	 * Ensures the specified resource is initialized.
	 * 
	 * @param resource
	 */
	public void ensureInitialized(IResource resource) {
		StateCacheFactory.getInstance().ensureInitialized(resource);
	}

	/**
	 * 
	 * Helper method that retrieves the branch name. Handles both win and unix
	 * versions.
	 * 
	 * @param version
	 * @return
	 */
	private String getBranchName(String version) {
		int firstBackSlash = 0;
		int lastBackSlash = 0;
		if (version.startsWith("\\")) {
			// Win32
			firstBackSlash = version.indexOf("\\");
			lastBackSlash = version.lastIndexOf("\\");

		} else {
			// Unix
			firstBackSlash = version.indexOf("/");
			lastBackSlash = version.lastIndexOf("/");
		}

		return version.substring(firstBackSlash, lastBackSlash + 1);
	}

	private int getCheckoutType() {
		if (ClearCasePlugin.isReservedCheckoutsAlways())
			return ClearCase.RESERVED;
		else if (ClearCasePlugin.isReservedCheckoutsIfPossible())
			return ClearCase.RESERVED_IF_POSSIBLE;
		else
			return ClearCase.UNRESERVED;
	}

	public void setOperationListener(OperationListener opListener) {
		this.opListener = opListener;
	}

	/**
	 * For a given element, calculates the final CC element that a checkout/in
	 * operation can act on. If the given cache points to a regular file or
	 * directory element, it is returned verbatim. If it is a symlink, we try to
	 * resolve the symlink to the final element and return a StateCache for
	 * that.
	 * 
	 * @param cache
	 *            a valid StateCache which maybe points to a symlink
	 * @return the final CC element, no symlink. If the symlink can't be
	 *         resolved in CC null is returned
	 */
	public StateCache getFinalTargetElement(StateCache cache) {
		if (!cache.isSymbolicLink() || null == cache.getSymbolicLinkTarget())
			return cache;
		File target = new File(cache.getSymbolicLinkTarget());
		if (!target.isAbsolute()) {
			target = null != cache.getPath() ? new File(cache.getPath())
					.getParentFile() : null;
			if (null != target) {
				target = new File(target, cache.getSymbolicLinkTarget());
			}
		}
		if (null != target && target.exists()) {
			IPath targetLocation = new Path(target.getAbsolutePath());
			IResource[] resources = null;
			if (target.isDirectory()) {
				resources = ResourcesPlugin.getWorkspace().getRoot()
						.findContainersForLocation(targetLocation);
			} else {
				resources = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocation(targetLocation);
			}
			if (null != resources) {
				for (int i = 0; i < resources.length; i++) {
					IResource foundResource = resources[i];
					ClearCaseProvider provider = ClearCaseProvider
							.getClearCaseProvider(foundResource);
					if (null != provider)
						return StateCacheFactory.getInstance().get(
								foundResource);
				}
			}
		}
		return null;
	}

	// FIXME: eraonel 20100503 move this to other file.
	public static boolean moveDirRec(File fromDir, File toDir) {
		if (!toDir.exists()) {
			return fromDir.renameTo(toDir);
		}

		File[] files = fromDir.listFiles();
		if (files == null) {
			return false;
		}

		boolean success = true;

		for (int i = 0; i < files.length; i++) {
			File fromFile = files[i];
			File toFile = new File(toDir, fromFile.getName());
			success = success && fromFile.renameTo(toFile);
		}

		fromDir.delete();

		return success;
	}

}
