package net.sourceforge.transparent.test;

import com.intellij.openapi.vcs.VcsException;
import junit.framework.TestCase;
import net.sourceforge.transparent.*;

import java.io.*;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import org.intellij.openapi.testing.MockProject;
import org.intellij.openapi.testing.MockLocalFileSystem;
import org.intellij.openapi.testing.MockVirtualFileManager;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.plugins.ExcludedPathsFromVcsConfiguration;
import org.intellij.plugins.ui.pathlisteditor.PathListElement;

public class TransparentVcsTest extends TestCase {
   private TransparentVcs                    vcs;
   private MockClearCase                     cc;
   private File                              dir;
   private File                              aFile;
   private TransparentConfiguration          transparentConfiguration;
   private String                            methodCalled;
   private ExcludedPathsFromVcsConfiguration excludedPathsConfiguration;

   public TransparentVcsTest(String s) {
      super(s);
   }

   protected void setUp() throws Exception {
      OpenApiFacade.localFileSystem = new MockLocalFileSystem();
      MockProject project = new MockProject();

      excludedPathsConfiguration = new ExcludedPathsFromVcsConfiguration(project);
      project.addComponent(excludedPathsConfiguration);

      transparentConfiguration                = new TransparentConfiguration(project);
      transparentConfiguration.implementation = MockClearCase.class.getName();
      project.addComponent(transparentConfiguration);

      vcs = new TransparentVcs(project) {
         public void refreshIDEA(ClearCaseFile file) {
         }

         public void transparentConfigurationChanged() {
            methodCalled = "transparentConfigurationChanged";
            super.transparentConfigurationChanged();
         }

         public void excludedPathsConfigurationChanged() {
            methodCalled = "excludedPathsConfigurationChanged";
            super.excludedPathsConfigurationChanged();
         }
      };

      vcs.projectOpened();

      cc = new MockClearCase();
      vcs.setClearCase(new ClearCaseDecorator(cc));

      dir   = createDir("tmp");
      aFile = createFile(dir, "A.java");
      dir.mkdir();
      assertTrue(dir.exists());

      cc.getElements().add(dir);
      cc.getElements().add(aFile);
   }

   protected void tearDown() throws Exception {
      recursiveDelete(dir);
   }

   public static void recursiveDelete(File file) {
      if (file.isDirectory()) {
         File[] files = file.listFiles();
         for (int i = 0; i < files.length; i++) {
            recursiveDelete(files[i]);
         }
      }
      assertTrue("couldn't delete " + file, file.delete());
   }

   public void testCheckInCheckOut() throws VcsException {
      assertStatusEquals(Status.CHECKED_IN, aFile);

      cc.assertSteps(new String[]{"checkout unreserved A.java",
                                  "checkin A.java",
                                  "checkout reserved A.java",
                                  "checkin A.java"});

      vcs.getTransparentConfig().checkoutReserved = false;
      vcs.checkoutFile("tmp/A.java", false);
      assertStatusEquals(Status.CHECKED_OUT, aFile);

      vcs.checkoutFile("tmp/A.java", false);
      vcs.checkinFile("tmp/A.java", "");
      assertStatusEquals(Status.CHECKED_IN, aFile);

      vcs.getTransparentConfig().checkoutReserved = true;
      vcs.checkoutFile("tmp/A.java", false);
      vcs.checkinFile("tmp/A.java", "");
      assertStatusEquals(Status.CHECKED_IN, aFile);

      cc.verifySteps();

   }


   public void testAddRemove() throws VcsException {
      assertStatusEquals(Status.CHECKED_IN, aFile);

      cc.assertSteps(new String[]{"checkout unreserved tmp",
                                  "delete A.java",
                                  "checkin tmp",
                                  "checkout unreserved tmp",
                                  "add file A.java",
                                  "checkin A.java",
                                  "checkin tmp"});
      vcs.removeFile(aFile.getPath(), "");

      assertStatusEquals(Status.NOT_AN_ELEMENT, aFile);
      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());

      vcs.addFile(aFile.getParent(), aFile.getName(), "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, aFile);
      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());

   }

   public void testAddFile_FullPath() throws Exception {
      File subDir     = createDir(dir, "nested");
      File subDirFile = createFile(subDir, "sub.txt");

      cc.assertSteps(new String[]{"checkout unreserved tmp",
                                  "add directory nested",
                                  "checkin nested",
                                  "checkin tmp",
                                  "checkout unreserved nested",
                                  "add file sub.txt",
                                  "checkin sub.txt",
                                  "checkin nested"});
      vcs.addFile(subDirFile.getParent(), subDirFile.getName(), "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, subDirFile);
      assertStatusEquals(Status.CHECKED_IN, subDir);
      assertStatusEquals(Status.CHECKED_IN, subDir.getParentFile());
   }

   public void testAddFile_ExcludedFile() throws Exception {
      vcs.getFileFilter().getExcludedPaths().add(new PathListElement(aFile.getParent(), false, false));

      vcs.addFile(aFile.getParent(), aFile.getName(), "");

      cc.assertSteps(new String[]{});
      cc.verifySteps();
   }

   public void testRemoveFile_CheckedOutFile() throws VcsException {

      cc.checkOut(aFile, false);
      assertStatusEquals(Status.CHECKED_OUT, aFile);

      cc.assertSteps(new String[]{"uncheckout A.java",
                                  "checkout unreserved tmp",
                                  "delete A.java",
                                  "checkin tmp"});

      vcs.removeFile(aFile.getPath(), "");

      assertStatusEquals(Status.NOT_AN_ELEMENT, aFile);
      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());

   }

   public void testRemoveFile_Tree() throws Exception {
      File file = new File("dir1/test.txt");
      File dir  = file.getParentFile();

      cc.getElements().add(dir);
      cc.getElements().add(file);

      cc.assertSteps(new String[]{});
      vcs.removeFile(file.getAbsolutePath(), "");

      cc.verifySteps();
   }

   public void testRemoveFile_WithAddingFileInSameTransaction() throws Exception {
      String expectedFileContents = "file\ncontent\n";
      File   file                 = createFile("./testDeleteFile.txt", expectedFileContents);
      new File(file.getPath() + TransparentVcs.TEMPORARY_FILE_SUFFIX).delete();

      cc.getElements().add(file.getParentFile());
      cc.getElements().add(file);

      vcs.removeFile(file.getPath(), "");

      assertStatusEquals(Status.NOT_AN_ELEMENT, file);
      assertTrue("File should still exist", file.exists());
      assertEquals("Contents of File are incorrect.", expectedFileContents, getFileContents(file));
   }

   public void testRemoveFile_ExcludedFile() throws Exception {
      vcs.getFileFilter().getExcludedPaths().add(new PathListElement(aFile.getParent(), false, false));

      vcs.removeFile(aFile.getPath(), "");

      cc.assertSteps(new String[]{});
      cc.verifySteps();
   }

   private String getFileContents(File fileToRemain) throws IOException {
      String       nextLine           = null;
      StringBuffer actualFileContents = new StringBuffer();

      Reader         reader         = null;
      BufferedReader bufferedReader = null;

      try {
         reader         = new FileReader(fileToRemain);
         bufferedReader = new BufferedReader(reader);
         while ((nextLine = bufferedReader.readLine()) != null) {
            actualFileContents.append(nextLine);
            actualFileContents.append("\n");
         }
      } finally {
         bufferedReader.close();
         reader.close();
      }
      return actualFileContents.toString();
   }

   private File createFile(String oldFileName, String fileContents) throws IOException {

      File file = new File(oldFileName);
      file.deleteOnExit();
      OutputStream outputStream = new FileOutputStream(file);
      outputStream.write(fileContents.getBytes());
      outputStream.close();
      return file;
   }

   // Add test for case where another file is in the way (with subpackaging as well)
   public void testRenameAndCheckInFile_CheckedInFile() throws Exception {
      File oldFileA = aFile;
      File newFileB = createFile(oldFileA.getParentFile(), "B.java");

      assertStatusEquals(Status.CHECKED_IN, oldFileA);
      assertStatusEquals(Status.NOT_AN_ELEMENT, newFileB);
      assertStatusEquals(Status.CHECKED_IN, oldFileA.getParentFile());

      cc.assertSteps(new String[]{"checkout unreserved tmp",
                                  "moving A.java to B.java",
                                  "checkin tmp"});
      vcs.renameAndCheckInFile(oldFileA.getPath(), "B.java", "");

      cc.verifySteps();

      assertStatusEquals(Status.NOT_AN_ELEMENT, oldFileA);
      assertStatusEquals(Status.CHECKED_IN, newFileB);
      assertStatusEquals(Status.CHECKED_IN, oldFileA.getParentFile());
   }

   public void testRenameAndCheckInFile_CheckedOutFile() throws Exception {
      File bFile = createFile(aFile.getParentFile(), "B.java");
      vcs.checkoutFile(aFile.getPath(), false);

      assertStatusEquals(Status.CHECKED_OUT, aFile);

      cc.assertSteps(new String[]{"checkin A.java",
                                  "checkout unreserved tmp",
                                  "moving A.java to B.java",
                                  "checkin tmp"});

      vcs.renameAndCheckInFile(aFile.getPath(), "B.java", "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, bFile);
      assertStatusEquals(Status.NOT_AN_ELEMENT, aFile);
   }

   public void testRenameAndCheckInFile_ExcludedFile() throws Exception {
      vcs.getFileFilter().getExcludedPaths().add(new PathListElement(aFile.getParent(), false, false));

      vcs.renameAndCheckInFile(aFile.getPath(), "newName", "");

      cc.assertSteps(new String[]{});
      cc.verifySteps();
   }

   public void testMoveRenameAndCheckInFile_CheckedInFile() throws Exception {
      File bFile = createBFileInFoobarDir();

      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());
      assertStatusEquals(Status.CHECKED_IN, aFile);

      cc.assertSteps(new String[]{"checkout unreserved tmp",
                                  "checkout unreserved foobar",
                                  "moving A.java to B.java",
                                  "checkin tmp",
                                  "checkin foobar"});

      vcs.moveRenameAndCheckInFile(aFile.getPath(), bFile.getParent(), "B.java", "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());
      assertStatusEquals(Status.NOT_AN_ELEMENT, aFile);
      assertStatusEquals(Status.CHECKED_IN, bFile.getParentFile());
      assertStatusEquals(Status.CHECKED_IN, bFile);

   }

   public void testMoveRenameAndCheckInFile_CheckedOutFile() throws Exception {
      File bFile = createBFileInFoobarDir();

      vcs.checkoutFile(aFile.getPath(), false);

      assertStatusEquals(Status.CHECKED_OUT, aFile);
      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());

      cc.assertSteps(new String[]{"checkin A.java",
                                  "checkout unreserved tmp",
                                  "checkout unreserved foobar",
                                  "moving A.java to B.java",
                                  "checkin tmp",
                                  "checkin foobar"
      });

      vcs.moveRenameAndCheckInFile(aFile.getPath(), bFile.getParent(), "B.java", "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());
      assertStatusEquals(Status.CHECKED_IN, bFile);
      assertStatusEquals(Status.CHECKED_IN, aFile.getParentFile());
      assertStatusEquals(Status.NOT_AN_ELEMENT, aFile);
   }

   public void testMoveRenameAndCheckInFile_ExcludedFile() throws Exception {
      vcs.getFileFilter().getExcludedPaths().add(new PathListElement(aFile.getParent(), false, false));

      vcs.moveRenameAndCheckInFile(aFile.getPath(), "c:/bla", "newName", "");

      cc.assertSteps(new String[]{});
      cc.verifySteps();
   }

   private File createBFileInFoobarDir() throws Exception {
      File bFile = createFile(createFile(dir, "foobar"), "B.java");

      cc.assertSteps(new String[]{"checkout unreserved tmp",
                                  "add directory foobar",
                                  "checkin foobar",
                                  "checkin tmp"});

      bFile.getParentFile().mkdir();
      vcs.addDirectory(bFile.getParentFile().getParent(), "foobar", "");
      vcs.checkinFile(aFile.getParent(), "");

      cc.verifySteps();

      assertStatusEquals(Status.CHECKED_IN, bFile.getParentFile());
      assertStatusEquals(Status.NOT_AN_ELEMENT, bFile);

      return bFile;
   }

   public void assertStatusEquals(Status status, File file) {
      assertEquals("status of " + file.getPath(), status, cc.getStatus(file));
   }

   private File createDir(File dir, String name) throws Exception {
      return createDir(new File(dir, name));
   }
   private File createDir(String path) throws Exception {
      return createDir(new File(path));
   }

   private File createDir(File file) {
      file.delete();
      file.deleteOnExit();
      if (!file.exists()) {
         assertTrue("Could not create " + file.getAbsolutePath(), file.mkdirs());
      }
      return file;
   }

   private File createFile(File dir, String name) throws Exception{
      File file = new File(dir, name);
      file.delete();
      file.deleteOnExit();
      createDir(dir.getAbsolutePath());
      if (!file.exists()) {
         assertTrue("Could not create " + file.getAbsolutePath(), file.createNewFile());
      }
      return file;
   }

   public void testProjectOpened() throws Exception {
      assertEquals("implementation", MockClearCase.class.getName(), vcs.getClearCase().getName());
      assertEquals("excludedPaths", Collections.EMPTY_LIST, vcs.getFileFilter().getExcludedPaths());
      OpenApiFacade.localFileSystem = new MockLocalFileSystem();
      excludedPathsConfiguration.addExcludedPath(new PathListElement("test", true, false));
      transparentConfiguration.implementation = CommandLineClearCase.class.getName();

      vcs.projectOpened();

      assertEquals("filter excluded paths", excludedPathsConfiguration.getExcludedPaths(),
                   vcs.getFileFilter().getExcludedPaths());
      assertEquals("implementation", CommandLineClearCase.class.getName(), vcs.getClearCase().getName());
      transparentConfiguration.notifyListenersOfChange();
      assertEquals("method called", "transparentConfigurationChanged", methodCalled);
      excludedPathsConfiguration.notifyListenersOfChange();
      assertEquals("method called", "excludedPathsConfigurationChanged", methodCalled);
   }

   public void testTransparentConfigurationChanged() throws Exception {
      assertEquals("implementation", MockClearCase.class.getName(), vcs.getClearCase().getName());
      transparentConfiguration.implementation = CommandLineClearCase.class.getName();
      vcs.transparentConfigurationChanged();
      assertEquals("implementation", CommandLineClearCase.class.getName(), vcs.getClearCase().getName());
   }

   public void testExcludedPathsConfigurationChanged() throws Exception {
      assertEquals("excludedPaths", Collections.EMPTY_LIST, vcs.getFileFilter().getExcludedPaths());
      OpenApiFacade.localFileSystem = new MockLocalFileSystem();
      List expectedExcludedPaths = Arrays.asList(new PathListElement[]{
         new PathListElement("test", true, false)
      });
      excludedPathsConfiguration.getExcludedPaths().addAll(expectedExcludedPaths);
      vcs.excludedPathsConfigurationChanged();

      assertEquals("excludedPaths", expectedExcludedPaths, vcs.getFileFilter().getExcludedPaths());
   }

    public void testStart() throws Exception {
        OpenApiFacade.virtualFileManager = new MockVirtualFileManager();
        vcs.start();
        assertNotNull("no modification listener", OpenApiFacade.virtualFileManager.modificationAttemptListener);
        assertNotNull("no virtual file listener", OpenApiFacade.virtualFileManager.virtualFileListener);
    }

}
