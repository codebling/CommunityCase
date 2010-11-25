package net.sourceforge.transparent;

import com.intellij.openapi.vfs.*;

import javax.swing.*;

import org.intellij.plugins.ExcludedFileFilter;
import org.intellij.plugins.util.FileFilter;

public class ClearCaseFileListener extends VirtualFileAdapter {
	private ClearCase clearcase = new CommandLineClearCase();
	private FileFilter filter = new ExcludedFileFilter();

	public FileFilter getFilter() {
		return filter;
	}

	public void fileCreated(VirtualFileEvent event) {
      if (event.getRequestor() == null || !filter.accept(event.getParent())) return;

		try {
			ClearCaseFile file = new ClearCaseFile(event.getFile(), clearcase);

			if (!file.isElement() && file.getParent().isElement()) {
				if (JOptionPane.showConfirmDialog(null, "add " + file.getName() + " to source code control?") == JOptionPane.YES_OPTION) {
					file.getParent().checkOut(false, false);
					file.add("");
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void fileDeleted(VirtualFileEvent event) {
      if (event.getRequestor() == null || !filter.accept(event.getParent())) return;

		try {
			ClearCaseFile file = new ClearCaseFile(event.getParent(), event.getFile().getName(), clearcase);

			if (file.isElement()) {
				file.undoCheckOut();
				file.getParent().checkOut(false, false);
				file.delete("Deleted " + file.getFile().getName());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void propertyChanged(VirtualFilePropertyEvent event) {
      if (!filter.accept(event.getFile()) || !event.getPropertyName().equals("name")) return;

		ClearCaseFile oldFile = new ClearCaseFile(event.getParent(), (String) event.getOldValue(), clearcase);
		ClearCaseFile newFile = new ClearCaseFile(event.getParent(), (String) event.getNewValue(), clearcase);

		move(oldFile, newFile);
	}

	public void fileMoved(VirtualFileMoveEvent event) {
      if (!filter.accept(event.getFile())) return;

		ClearCaseFile oldFile = new ClearCaseFile(event.getOldParent(), event.getFile().getName(), clearcase);
		ClearCaseFile newFile = new ClearCaseFile(event.getNewParent(), event.getFile().getName(), clearcase);

		move(oldFile, newFile);
	}

	private void move(ClearCaseFile oldFile, ClearCaseFile newFile) {
		try {
			if (oldFile.isElement()) {
				moveFileBack(newFile, oldFile);

				oldFile.checkIn("");
				oldFile.move(newFile, "");
				newFile.checkOut(false, false);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void moveFileBack(ClearCaseFile newFile, ClearCaseFile oldFile) {
		newFile.getFile().renameTo(oldFile.getFile());
	}
}
