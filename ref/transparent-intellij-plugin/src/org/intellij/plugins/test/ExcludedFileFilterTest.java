package org.intellij.plugins.test;

import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import org.intellij.openapi.testing.MockVirtualFile;
import org.intellij.plugins.ExcludedFileFilter;
import org.intellij.plugins.ui.pathlisteditor.PathListElement;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.openapi.testing.MockLocalFileSystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExcludedFileFilterTest extends TestCase {
   private static PathListElement[] EXCLUDED_PATHS;

   private ExcludedFileFilter filter;
   private String             NON_RECURSIVE_DIRECTORY = "C:\\src\\non_recursive";
   private String             RECURSIVE_DIRECTORY     = "C:\\src\\recursive";
   private String             FILE                    = "C:\\src\\somefile.txt";
   private String FILE_DEEP_INSIDE_NON_RECURSIVE_DIRECTORY = NON_RECURSIVE_DIRECTORY + "\\a\\b\\c\\aFile.java";

   public ExcludedFileFilterTest(String s) {
      super(s);
   }

   protected void setUp() throws Exception {
      OpenApiFacade.localFileSystem = new MockLocalFileSystem();
      EXCLUDED_PATHS = new PathListElement[]{
         new PathListElement("C:\\src\\stellj_newtask", true, false),
         new PathListElement(RECURSIVE_DIRECTORY, true, false),
         new PathListElement(NON_RECURSIVE_DIRECTORY, false, false),
         new PathListElement(FILE, false, true),
         new PathListElement(FILE_DEEP_INSIDE_NON_RECURSIVE_DIRECTORY,false,true)
      };

      filter = new TestFilter(Arrays.asList(EXCLUDED_PATHS), Collections.EMPTY_LIST);
   }

   protected void tearDown() throws Exception {
      filter = null;
   }


   public void testAddExcludedPath() {
      String PATH1 = "c:\\src\\";
      filter = new TestFilter(Arrays.asList(new String[]{PATH1}), Collections.EMPTY_LIST);
      assertEquals("excluded path size", 1, filter.getExcludedPaths().size());
      assertEquals("path 1", PATH1, filter.getExcludedPaths().get(0));
   }

   public void testAccept_FileInExcludedDirectory() throws Exception {
      for (int i = 0; i < EXCLUDED_PATHS.length; i++) {
         PathListElement pathListElement = EXCLUDED_PATHS[i];
         if (!pathListElement.isFile()) {
            String      fileName = pathListElement.getPresentableUrl() + "\\Test.java";
            VirtualFile vfile    = new MockVirtualFile(fileName, false);
            assertFalse("should filter out " + fileName, filter.accept(vfile));
         }
      }
   }

   public void testAccept_ExcludedDirectories() throws Exception {
      for (int i = 0; i < EXCLUDED_PATHS.length; i++) {
         PathListElement pathListElement = EXCLUDED_PATHS[i];
         if (!pathListElement.isFile()) {
            String      fileName = pathListElement.getPresentableUrl();
            VirtualFile vfile    = new MockVirtualFile(fileName, true);
            assertFalse("should filter out " + fileName, filter.accept(vfile));
         }
      }
   }

   public void testAccept_FileMatchingExcludedFile() throws Exception {
      VirtualFile vfile = new MockVirtualFile(FILE, false);
      assertFalse("should filter out " + FILE, filter.accept(vfile));
   }

   public void testAccept_FileMatchingExcludedFileInNonRecursiveDirectory() throws Exception {
      VirtualFile vfile = new MockVirtualFile(FILE_DEEP_INSIDE_NON_RECURSIVE_DIRECTORY, false);
      assertFalse("should filter out " + FILE_DEEP_INSIDE_NON_RECURSIVE_DIRECTORY, filter.accept(vfile));
   }

   public void testAccept_OutsideExcludedPath() throws Exception {
      String      fileName = "c:\\src\\Test.java";
      VirtualFile vfile    = new MockVirtualFile(fileName, false);
      assertTrue("should accept " + fileName, filter.accept(vfile));
   }

   public void testAccept_FileInSubDirectoryOfExcludedNonRecursiveDirectory() throws Exception {
      String      nameOfFileInSubDirectory = NON_RECURSIVE_DIRECTORY + "\\someDir\\Test.java";
      VirtualFile vfile                    = new MockVirtualFile(nameOfFileInSubDirectory, false);
      assertTrue("should accept " + nameOfFileInSubDirectory, filter.accept(vfile));
   }

   public void testAccept_FileInSubDirectoryOfExcludedRecursiveDirectory() throws Exception {
      String      nameOfFileInSubDirectory = RECURSIVE_DIRECTORY + "\\someDir\\Test.java";
      VirtualFile vfile                    = new MockVirtualFile(nameOfFileInSubDirectory, false);
      assertFalse("should not accept " + nameOfFileInSubDirectory, filter.accept(vfile));
   }

   public void testAccept_ExcludedFile() throws Exception {
//      assertFalse("should not accept " + FILE,)
   }

   private class TestFilter extends ExcludedFileFilter {
      public TestFilter(List excludedPaths, List excludedExtensions) {
         super(excludedPaths, excludedExtensions);
      }

      public String getCanonicalPath(VirtualFile file) throws IOException {
         return file.getPath();
      }
   }

}
