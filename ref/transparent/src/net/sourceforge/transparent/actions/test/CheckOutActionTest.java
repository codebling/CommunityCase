package net.sourceforge.transparent.actions.test;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.MockPresentationFactory;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.openapi.testing.*;
import org.intellij.openapi.testing.fileEditor.MockFileDocumentManager;
import org.intellij.plugins.util.testing.MockCommandUtil;
import net.sourceforge.transparent.Status;
import net.sourceforge.transparent.TransparentVcs;
import net.sourceforge.transparent.actions.ActionContext;
import net.sourceforge.transparent.actions.CheckOutAction;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class CheckOutActionTest extends TestCase {

// Temp Change
   public HashMap dataContextMap = new HashMap();

   ActionContext context;

   AnActionEvent event;

   ArrayList checkedOutFiles = new ArrayList();
   ArrayList expectedCheckedOutFiles = new ArrayList();

   CheckOutAction action;
   boolean isRecursive = true;
   Status status = Status.CHECKED_IN;

   class MockInputEvent extends MouseEvent {
      public MockInputEvent() {
         super(new Container(), 0, 0, 0, 0, 0, 1, false);
      }
   };


   protected void setUp() throws Exception {
//      OpenApiFacade.application = new MockApplication();
      OpenApiFacade.fileStatusManager = new MockFileStatusManager();
      OpenApiFacade.actionManager = new MockActionManager();
      OpenApiFacade.fileDocumentManager = new MockFileDocumentManager();
      context = new ActionContext();
      context.dataContext = new MockDataContext(dataContextMap);
      MockProject project = new MockProject();
      context.project = project;
      MockAbstractVcsHelper vcsHelper = new MockAbstractVcsHelper();
      project.addComponent(vcsHelper);
      context.vcsHelper = vcsHelper;
      context.vcs = new TransparentVcs(context.project) {
         public boolean checkoutFile(String path, boolean keepHijacked) throws VcsException {
            if (!expectedCheckedOutFiles.contains(path)) {fail("should not try to checkout " + path);}
            checkedOutFiles.add(path);
            return true;
         }

         public Status getFileStatus(VirtualFile file) {
            return status;
         }
      };

      action = new CheckOutAction(new MockCommandUtil()) {
         public boolean isEnabled(ActionContext context) {
            return true;
         }

          protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
              return true;
          }

         public ActionContext getActionContext(AnActionEvent e) {
            assertSame("event", event, e);
            return context;
         }

         protected boolean askIfShouldRecurse(ActionContext context) {
            return isRecursive;
         }

         protected String getActionName(ActionContext context) {
            return "Check out";
         }
      };

      event = new AnActionEvent(new MockInputEvent(),
                                context.dataContext,
                                "edit",
                                MockPresentationFactory.newInstance(),
                                1);
   }

   public void testPerform_Checkout0File() throws Exception {

      context.files = createVirtualFileArray(expectedCheckedOutFiles);

      action.actionPerformed(event);

      assertTrue("checkoutFile is called", checkedOutFiles.isEmpty());
   }

   public void testPerform_Checkout1File() throws Exception {
      expectedCheckedOutFiles.add("c:\\file.java");

      context.files = createVirtualFileArray(expectedCheckedOutFiles);

      action.actionPerformed(event);
      assertFalse("checkoutFile is not called", checkedOutFiles.isEmpty());
      assertEquals("file status not refreshed",
                   "c:\\file.java",
                   OpenApiFacade.fileStatusManager.filePassedIn.getPath());
      assertEquals("FileStatusManager.refreshFileStatus not called",
                   "refreshFileStatus",
                   OpenApiFacade.fileStatusManager.methodCalled);
   }

   public void testPerform_Checkout3File() throws Exception {
      expectedCheckedOutFiles.add("c:\\file1.java");
      expectedCheckedOutFiles.add("c:\\file2.java");
      expectedCheckedOutFiles.add("c:\\file3.java");

      context.files = createVirtualFileArray(expectedCheckedOutFiles);

      action.actionPerformed(event);

      assertEquals("files checked out", expectedCheckedOutFiles, checkedOutFiles);
   }

   public void testCheckout_DirRecursive_NotChangedStatus() throws Exception {
      setupCheckoutDir();

      expectedCheckedOutFiles.add("c:\\test.java");
      expectedCheckedOutFiles.add("c:\\test");
      expectedCheckedOutFiles.add("c:\\test\\test1.java");
      expectedCheckedOutFiles.add("c:\\test\\test2.java");

      isRecursive = true;

      action.actionPerformed(event);

      assertEquals("files checked out", expectedCheckedOutFiles, checkedOutFiles);
   }

   public void testCheckout_DirNotRecursive_NotChangedStatus() throws Exception {
      setupCheckoutDir();

      expectedCheckedOutFiles.add("c:\\test.java");
      expectedCheckedOutFiles.add("c:\\test");

      isRecursive = false;

      action.actionPerformed(event);

      assertEquals("files checked out", expectedCheckedOutFiles, checkedOutFiles);
   }

//   public void testCheckout_DirNotRecursive_AddedStatus() throws Exception {
//      context.files = new VirtualFile[] {new MockVirtualFile("c:\\test.java", false, FileStatus.MODIFIED),
//                                           new MockVirtualFile("c:\\test", true)};
//
//      expectedCheckedOutFiles.add("c:\\test");
//
//      isRecursive = false;
//
//      action.actionPerformed(event);
//
//      assertEquals("files checked out", expectedCheckedOutFiles, checkedOutFiles);
//   }

   private void setupCheckoutDir() {
      MockVirtualFile dir = new MockVirtualFile("c:\\test", true);
      dir.children = new VirtualFile[] { new MockVirtualFile("c:\\test\\test1.java", false),
                                           new MockVirtualFile("c:\\test\\test2.java", false) };
      context.files = new VirtualFile[] {new MockVirtualFile("c:\\test.java", false),
                                           dir};
   }

   private VirtualFile[] createVirtualFileArray(ArrayList paths) {
      VirtualFile[] files = new VirtualFile[paths.size()];
      for (int i = 0; i < paths.size(); i++) {
         files[i] = new MockVirtualFile((String) paths.get(i), false);
      }
      return files;
   }

}
