package net.sourceforge.transparent.test;

import junit.framework.TestCase;
import net.sourceforge.transparent.CheckedOutStatus;
import net.sourceforge.transparent.ClearCase;
import net.sourceforge.transparent.Status;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Feb 19, 2003
 * Time: 2:39:38 PM
 * To change this template use Options | File Templates.
 */
public abstract class ClearCaseTestFixture extends TestCase {
   private ClearCase _cc;
   private File dir;
   private File file;

   public ClearCaseTestFixture(ClearCase _cc) {
      this._cc = _cc;
   }

   public void testUndocheckoutDirRemoveAddedFile() {
   }

   public void testDelete() {
      _cc.delete(file, "");
      assertTrue("file is an element after delete", !_cc.isElement(file));
   }

   public void testCheckin() {
      _cc.checkIn(file, "bla");
      assertTrue("file is checked out after checkin", !_cc.isCheckedOut(file));
   }

   public void testMove() {
      File movedFile = new File(dir.getPath(), "movedfile.txt");
      try {
         _cc.move(file, movedFile, "");
         assertTrue("old file is an element after move", !_cc.isElement(file));
         assertTrue("new file is not an element after move", _cc.isElement(movedFile));
      } finally {
         movedFile.delete();
      }
   }

   public void testUpdateNotRemovingHijacked() {
      _cc.checkIn(file, "Testing Update on Hijacked.");
      assertTrue("dir no longer check out", _cc.isCheckedOut(dir));
      assertFalse("file is check out", _cc.isCheckedOut(file));
      file.delete();
      try {
         file.createNewFile();
         assertEquals("file is not hijacked", Status.HIJACKED, _cc.getStatus(file));
      } catch (IOException e) {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
      }

      _cc.cleartool("update " + file.getPath());
      assertEquals("file is not hijacked after update", Status.HIJACKED, _cc.getStatus(file));
   }

   public void testReservedUnReserved() {
      _cc.checkIn(file, "Checked In unReserved.");
      _cc.checkOut(file, true);
      assertEquals("file is not checked out reserved", CheckedOutStatus.RESERVED, _cc.getCheckedOutStatus(file));
   }

   public void setUp() throws Exception {
      System.out.println("ClearCaseTestFixture.setUp");

      dir = new File(getTestDirPath());

      if (_cc.isCheckedOut(dir)) {
         _cc.undoCheckOut(dir);
      }

      assertFalse("dir is checked out before check out", _cc.isCheckedOut(dir));
      _cc.checkOut(dir, false);
      // TODO: should add test to check the reserved state
      assertTrue("dir is not checked out after checkout", _cc.isCheckedOut(dir));
      assertEquals("dir is not checked out unreserved", CheckedOutStatus.UNRESERVED, _cc.getCheckedOutStatus(dir));

      file = new File(dir.getPath(), "added.txt");
      file.createNewFile();
      assertFalse("file is an element before add", _cc.isElement(file));
      _cc.add(file, "");
      assertTrue("file is not an element after add", _cc.isElement(file));
      System.out.println("*** leaving ClearCaseTestFixture.setUp");
   }

   public void tearDown() {
      System.out.println("ClearCaseTestFixture.tearDown");
      _cc.undoCheckOut(dir);
      assertFalse("dir is checked out after undo checkout", _cc.isCheckedOut(dir));
      assertEquals("dir is not checked out", CheckedOutStatus.NOT_CHECKED_OUT, _cc.getCheckedOutStatus(dir));
      assertTrue("file is an element anymore after undo checkout", !_cc.isElement(file));
      file.delete();
      System.out.println("leaving ClearCaseTestFixture.tearDown");
   }

   public static String getTestDirPath() throws Exception {
      String dirPath = null;
      ResourceBundle rs = ResourceBundle.getBundle("clearcase");
      String userName = System.getProperty("user.name");
      InetAddress inetAddress = InetAddress.getLocalHost();
      String userKey = "clearcase_dir." + userName + "." + inetAddress.getHostName();
      try {
         dirPath = rs.getString(userKey);

      } catch (Throwable te) {
         try {
            dirPath = rs.getString("clearcase_dir.userid.hostname");
         } catch (Throwable tee) {
            fail("'clearcase_dir.userid.hostname' nor '" + userKey +
                 "' not found in clearcase.properties file \n" + tee.getMessage());
         }
      }


      return dirPath;
   }
}
