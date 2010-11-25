package net.sourceforge.transparent.test;

/**
 * User: sg426575
 * Date: Aug 29, 2003
 * Time: 10:01:40 AM
 */

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import net.sourceforge.transparent.*;
import org.intellij.openapi.testing.MockAbstractVcsHelper;
import org.intellij.openapi.testing.MockProject;
import org.intellij.openapi.testing.MockVirtualFile;
import org.intellij.plugins.util.FileUtil;
import org.intellij.plugins.util.testing.MockCommandUtil;
import org.intellij.plugins.util.testing.MockFileUtil;

import java.io.IOException;
import java.io.File;

public class CheckOutHelperTest extends TestCase {
   CheckOutHelper object;
   private TransparentVcs vcs;
   private MockProject project;
   private TransparentConfiguration config;
   private CheckOutHelper helper;
   private MockAbstractVcsHelper vcsHelper;
   private boolean checkoutFileThrowsException;
   private MockFileUtil fileUtil;
   private boolean fileCheckedOut;
   private MockClearCase clearcase;
   public static final String FILE_PATH = "test";
   public static final MockVirtualFile FILE = new MockVirtualFile(FILE_PATH, false);

   public void setUp() {
      clearcase = new MockClearCase(new File[] {new File(FILE_PATH)});
      vcsHelper = new MockAbstractVcsHelper();
      project = new MockProject();
      vcs = new TransparentVcs(project) {
         public boolean checkoutFile(String path, boolean keepHijacked) throws VcsException {
            assertEquals("file", FILE_PATH, path);
            if (checkoutFileThrowsException) throw new VcsException("checkout");
            fileCheckedOut = true;

            return true;
         }
         public ClearCase getClearCase() {
            return clearcase; }
      };
      config = new TransparentConfiguration(project);
      fileUtil = new MockFileUtil();
      helper = new CheckOutHelper(vcs, vcsHelper, config, fileUtil);
   }

   public void testCheckOutOrHijackFile_Offline() throws Exception {
      config.offline = true;
      helper.checkOutOrHijackFile(FILE);
      assertNoException();
      assertTrue("file is not writable", fileUtil.writable);
   }

   private void assertNoException() {
      if (!vcsHelper.errors.isEmpty())
         fail("unexpected exception " + ((Throwable)vcsHelper.errors.get(0)).getMessage());
   }

   public void testCheckOutOrHijackFile_Online_Element() throws Exception {
      config.offline = false;
      helper.checkOutOrHijackFile(FILE);
      assertTrue("no exception", vcsHelper.errors.isEmpty());
      assertTrue("file is checked out", fileCheckedOut);
   }

   public void testCheckOutOrHijackFile_Online_PrivateFile() throws Exception {
      clearcase = new MockClearCase(new File[] {});
      config.offline = false;
      assertFalse("file is private", clearcase.isElement(new File(FILE_PATH)));

      helper.checkOutOrHijackFile(CheckOutHelperTest.FILE);
      
      assertTrue("no exception", vcsHelper.errors.isEmpty());
      assertFalse("file is checked out", fileCheckedOut);
   }

   public void testCheckOutOrHijackFile_Online_CheckOutError() throws Exception {
      config.offline = false;
      checkoutFileThrowsException = true;
      helper.checkOutOrHijackFile(FILE);
      assertEquals("exception count", 1, vcsHelper.errors.size());
      assertFalse("file is checked out", fileCheckedOut);
   }

   public void testCheckOutOrHijackFile_Offline_HijackError() throws Exception {
      config.offline = true;
      fileUtil.setFileWritableThrowsException = true;
      helper.checkOutOrHijackFile(FILE);
      assertEquals("exception count", 1, vcsHelper.errors.size());
      assertFalse("file is writable", fileUtil.writable);
   }

}