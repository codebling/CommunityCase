package net.sourceforge.transparent.test;

import junit.framework.Assert;
import net.sourceforge.transparent.ClearCase;
import net.sourceforge.transparent.Status;

import java.io.File;
import java.io.IOException;

public class ClearCaseImplTester extends Assert {
	private ClearCase _cc;

	public ClearCaseImplTester(ClearCase cc) {
		_cc = cc;
	}

	public void test(File base) throws IOException {
		checkout(base);

		File dir = new File(base, "foobar");
		dir.mkdir();

		addDir(dir);

		File file = new File(dir, "A.java");
		file.createNewFile();

		addFile(file);

		File newFile = new File(dir, "B.java");

		move(file, newFile);

		checkin(newFile);

		removeFile(newFile);

		_cc.undoCheckOut(dir);

		removeDir(dir);

		undoCheckout(base);
	}

	private void checkin(File file) {
		_cc.checkIn(file, "");
		assertEquals(Status.CHECKED_IN, _cc.getStatus(file));
	}

	private void undoCheckout(File base) {
		_cc.undoCheckOut(base);
		assertEquals(Status.CHECKED_IN, _cc.getStatus(base));
	}

	private void removeDir(File dir) {
		_cc.delete(dir, "");
		assertEquals(Status.NOT_AN_ELEMENT, _cc.getStatus(dir));
	}

	private void removeFile(File newFile) {
		_cc.delete(newFile, "");
		assertEquals(Status.NOT_AN_ELEMENT, _cc.getStatus(newFile));
	}

	private void move(File file, File newFile) {
		_cc.move(file, newFile, "");
		assertEquals(Status.NOT_AN_ELEMENT, _cc.getStatus(file));
	}

	private void addFile(File file) {
		_cc.add(file, "");

		assertEquals(Status.CHECKED_OUT, _cc.getStatus(file));
	}

	private void addDir(File dir) {
		assertEquals(Status.NOT_AN_ELEMENT, _cc.getStatus(dir));
		_cc.add(dir, "");
		assertEquals(Status.CHECKED_OUT, _cc.getStatus(dir));
	}

	private void checkout(File base) {
		_cc.checkOut(base, false);
		assertEquals(Status.CHECKED_OUT, _cc.getStatus(base));
	}
}
