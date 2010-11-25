package net.sourceforge.eclipseccase;

import java.io.File;

import net.sourceforge.clearcase.ClearCase;
import net.sourceforge.clearcase.ClearCaseElementState;
import net.sourceforge.clearcase.events.OperationListener;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

public class ViewprivOperationListener implements OperationListener {

	private static final String TRACE_ID = ViewprivOperationListener.class
			.getSimpleName();

	private IProgressMonitor monitor = null;

	private int receivedLines = 0;

	private String prefix;

	private boolean isGatheringCO;

	public ViewprivOperationListener(String prefix, boolean isGatheringCO,
			IProgressMonitor monitor) {
		this.monitor = monitor;
		this.prefix = prefix;
		this.isGatheringCO = isGatheringCO;
		updateJobStatus();
	}

	public void finishedOperation() {
	}

	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	public void ping() {
	}

	public void print(String filename) {
		receivedLines++;

		if (monitor.isCanceled())
			throw new OperationCanceledException();

		if (receivedLines % 50 == 0) {
			updateJobStatus();
		}
		if (filename.length() == 0) {
			// ignore empty filenames
			return;
		}
		if (filename.charAt(0) == '#') {
			// ignore filenames starting with #, as these are in non-mounted
			// VOBs on PC
			return;
		}
		// we have a valid name now
		// System.out.println("+++ "+ filename);

		File targetLocation = new File(filename);
		IResource[] resources = null;
		if (targetLocation.isDirectory()) {
			resources = ResourcesPlugin.getWorkspace().getRoot()
					.findContainersForLocationURI(targetLocation.toURI());
		} else {
			resources = ResourcesPlugin.getWorkspace().getRoot()
					.findFilesForLocationURI(targetLocation.toURI());
		}

		// What about found resources that are not visible in
		// workspace yet? Two possibilities:
		// 1) If it would be visible after a manual refresh, we get a
		// valid IResource here, but resource.isAccessible() is false.
		// That is handled in cache.doUpdate() later
		// 2) If the workspace does not have a possible access path to the
		// resource, resources is empty, the for loop is not executed
		for (IResource resource : resources) {
			StateCache cache = StateCacheFactory.getInstance().getWithNoUpdate(
					resource);
			if (cache.isUninitialized()) {
				if (isGatheringCO) {
					trace("Found new CO(1) " + resource.getLocation());
					cache.doUpdate();
				} else {
					trace("Found new ViewPriv " + resource.getLocation());
				}
				cache.updateAsync(true);
			} else if (cache.isClearCaseElement()) {
				if (isGatheringCO) {
					if (cache.isCheckedOut()) {
						// validate that this is still a checkedout element
						cache.setVpStateVerified();
					} else {
						trace("Found new CO(2) " + resource.getLocation());
						cache.updateAsync(true);
					}
				} else {
					trace("Found ViewPriv, but cache wrong "
							+ resource.getLocation());
					cache.updateAsync(true);
				}
			} else {
				// cached state is not (yet) a CC element
				if (isGatheringCO) {
					trace("Found new CO(3) " + resource.getLocation());
					cache.updateAsync(true);
				} else if (cache.isViewprivate()){
					// validate that this is still a private element
					cache.setVpStateVerified();
				}
			}
		}
	}

	private void updateJobStatus() {
		monitor.subTask(prefix + ", lines received from CC: " + receivedLines);
	}

	public void printErr(String msg) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public void printInfo(String msg) {
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}

	public void startedOperation(int amountOfWork) {
	}

	public void worked(int ticks) {
	}

	private void trace(String message) {
		if (ClearCasePlugin.DEBUG_STATE_CACHE) {
			ClearCasePlugin.trace(TRACE_ID, message);
		}
	}
}
