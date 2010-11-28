package net.sourceforge.transparent.actions.checkin.test;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import net.sourceforge.transparent.actions.checkin.*;
import org.intellij.openapi.testing.MockProject;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Mar 10, 2003
 * Time: 2:48:16 PM
 * To change this template use Options | File Templates.
 */
public class CheckInOptionsHandlerTest extends TestCase {
   private MockProject project;
   private StringBuffer methodsCalledLogString;
   private CheckInConfig config;
   private CheckInOptionsHandler handler;
   private TestCheckInFileDialog dialog;

   protected void setUp() {
      project = new MockProject();
      methodsCalledLogString = new StringBuffer();
      config = new CheckInConfig(project) {
         public void copyToDialog(CheckInFields fields) throws VcsException {
            methodsCalledLogString.append("copyToDialog() ");
         }

         public void copyFromDialog(CheckInFields fields) throws VcsException {
            methodsCalledLogString.append("copyFromDialog() ");
         }
      };
      dialog = new TestCheckInFileDialog(project,config.getCheckInEnvironment());
      handler = new CheckInOptionsHandler();
   }

   public void testAskForCheckInConfirmation_Ok() throws Exception {
      dialog.comment = "Comment";
      dialog.exitCode = CheckInFileDialog.OK_EXIT_CODE;
      boolean isOk = handler.askForCheckInConfirmation(dialog, config);
      assertTrue("Should be ok", isOk);
      assertFalse("Check in For All Chosen", handler.isForAllChosen());
      assertEquals("Comments", "Comment", handler.getComment());
      assertMethodsCalled("copyToDialog() show() copyFromDialog() ");
   }

   public void testAskForCheckInConfirmation_Cancel() throws Exception {
      dialog.comment = "Comment";
      dialog.exitCode = CheckInFileDialog.CANCEL_EXIT_CODE;
      boolean isOk = handler.askForCheckInConfirmation(dialog, config);
      assertFalse("Should not be ok", isOk);
      assertFalse("Check in For All Chosen", handler.isForAllChosen());
      assertMethodsCalled("copyToDialog() show() ");
   }

   public void testAskForCheckInConfirmation_OkAll() throws Exception {
      dialog.comment = "Comment";
      dialog.exitCode = CheckInFileDialog.OK_ALL_EXIT_CODE;
      boolean isOk = handler.askForCheckInConfirmation(dialog, config);
      assertTrue("Should be ok for first file", isOk);
      assertTrue("Check in For All Chosen on first file", handler.isForAllChosen());
      assertEquals("Comments", "Comment", handler.getComment());
      assertMethodsCalled("copyToDialog() show() copyFromDialog() ");
      resetMethodCalledLogString();

      isOk = handler.askForCheckInConfirmation(dialog, config);
      assertTrue("Should be ok for subsequent file", isOk);
      assertTrue("Check in For All Chosen on first file", handler.isForAllChosen());
      assertEquals("Comments", "Comment", handler.getComment());

      assertMethodsCalled("");
   }

   private void resetMethodCalledLogString() {
      methodsCalledLogString.delete(0, methodsCalledLogString.length());
   }

   private void assertMethodsCalled(String expectedMethodsCalledLog) {
      assertEquals("method called", expectedMethodsCalledLog, methodsCalledLogString.toString());
   }

   public class TestCheckInFileDialog extends CheckInFileDialog {
      public int exitCode;
      public String comment;

      public TestCheckInFileDialog(Project project, CheckInEnvironment env) {
         super(project, new VirtualFile[0], env, 0);
      }

      public void show() {
         methodsCalledLogString.append("show() ");
      }

      public int getExitCode() { return exitCode; }
      public String getComment() { return comment;}
   }


}
