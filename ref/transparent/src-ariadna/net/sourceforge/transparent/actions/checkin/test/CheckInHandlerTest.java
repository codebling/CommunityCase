package net.sourceforge.transparent.actions.checkin.test;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.project.Project;
import junit.framework.TestCase;
import org.intellij.openapi.testing.MockProject;
import net.sourceforge.transparent.TransparentConfiguration;
import net.sourceforge.transparent.TransparentVcs;
import net.sourceforge.transparent.actions.checkin.BaseCheckInDialog;
import net.sourceforge.transparent.actions.checkin.CheckInHandler;
import net.sourceforge.transparent.actions.checkin.CheckInProjectDialog;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Mar 10, 2003
 * Time: 2:48:16 PM
 * To change this template use Options | File Templates.
 */
public class CheckInHandlerTest extends TestCase {
    private static final String COMMENT_STRING = "User Entered Comment.";
    private static final String ORIGINAL_COMMENT_STRING = "This is the original comment.";
    private MockProject project;
    private TransparentVcs transparentVcs;
    private StringBuffer methodsCalledLogString;
    private CheckInHandler handler;
    private boolean shouldShowDialog = false;
    private int expectedExitCode = BaseCheckInDialog.OK_EXIT_CODE;

    public void testAskForCheckInConfirmation_Ok() throws Exception {
        handler.setDialogResult(BaseCheckInDialog.OK_EXIT_CODE);
        handler.getCheckInDialog().getCommentArea().setText(COMMENT_STRING);
        assertTrue("Check in Confirmation Cancelled", handler.askForCheckInConfirmation());
        assertFalse("Check in For All Chosen", handler.isForAllChosen());
        assertEquals("Comment Field", COMMENT_STRING, handler.getComment());
        assertEquals("Methods Called", "showDialog writeScr ", methodsCalledLogString.toString());
    }

    public void testAskForCheckInConfirmation_Cancel() throws Exception {
        handler.getCheckInDialog().getCommentArea().setText(COMMENT_STRING);
        handler.setDialogResult(BaseCheckInDialog.CANCEL_EXIT_CODE);
        assertFalse("Check in Confirmation Not Cancelled", handler.askForCheckInConfirmation());
        assertEquals("Comment Field", ORIGINAL_COMMENT_STRING, handler.getComment());
        assertEquals("Methods Called", "showDialog ", methodsCalledLogString.toString());
    }

    public void testAskForCheckInConfirmation_OkAllFirstFile() throws Exception {
        handler.getCheckInDialog().getCommentArea().setText(COMMENT_STRING);
        handler.setDialogResult(BaseCheckInDialog.OK_ALL_EXIT_CODE);
        assertTrue("Check in Confirmation Cancelled", handler.askForCheckInConfirmation());
        assertEquals("Comment Field", COMMENT_STRING, handler.getComment());
        assertTrue("Check in Not For All Chosen", handler.isForAllChosen());
        assertEquals("Methods Called", "showDialog writeScr ", methodsCalledLogString.toString());
    }

    public void testAskForCheckInConfirmation_OkAllSubsequentFiles() throws Exception {
        handler.getCheckInDialog().getCommentArea().setText(COMMENT_STRING);
        handler.setDialogResult(BaseCheckInDialog.OK_ALL_EXIT_CODE);
        handler.askForCheckInConfirmation();
       resetMethodCalledLogString();

       assertTrue("Check in Confirmation Cancelled", handler.askForCheckInConfirmation());
        assertEquals("Comment Field", COMMENT_STRING, handler.getComment());
        assertEquals("Methods Called", "", methodsCalledLogString.toString());
    }

   private void resetMethodCalledLogString()
   {
      methodsCalledLogString.delete(0, methodsCalledLogString.length());
   }

   protected void setUp() {
        project = new MockProject();
        transparentVcs = new TransparentVcs(project);
        methodsCalledLogString = new StringBuffer();
        handler = new TestCheckInHandler();
        handler.setComment(ORIGINAL_COMMENT_STRING);
    }

    public class MockTransparentConfiguration extends TransparentConfiguration {

    }

   public void testWriteScr_FileSpecified() throws Exception
   {
      assertScrWrittenCorrectly("Scr234", "C:/temp/scrTextFileName.txt");
   }

   public void testWriteScr_NoFileSpecified() throws Exception
   {
      assertScrWrittenCorrectly("", "");
   }

   private void assertScrWrittenCorrectly(String expectedScr, String scrFileName) throws VcsException
   {
      MockTransparentConfiguration configuration = new MockTransparentConfiguration();
      project.addComponent(configuration);
      configuration.scrTextFileName = scrFileName;
      transparentVcs.initTransparentConfiguration();

      final StringWriter stringWriter = new StringWriter();
      CheckInHandler checkInDialogHandler = new CheckInHandler(transparentVcs)
      {
         protected Writer createWriter(String scrTextFileName) throws IOException
         {
            return stringWriter;
         }
      };

      checkInDialogHandler.setScr("Scr234");
      checkInDialogHandler.writeScr();

      assertEquals("Scr Number Equals", expectedScr, stringWriter.toString());
   }


    public class TestCheckInHandler extends CheckInHandler {

        public TestCheckInHandler() {
            super(transparentVcs);
            checkInDialog = new TestCheckInProjectDialog(project);
        }


        public void showDialog() throws VcsException {
            methodsCalledLogString.append("showDialog ");
        }

        public void writeScr() throws VcsException {
            methodsCalledLogString.append("writeScr ");
        }
    }
   public class TestCheckInProjectDialog extends CheckInProjectDialog
   {
      public TestCheckInProjectDialog(Project project)
      {
         super(project, null);
      }

      public boolean shouldShowDialog() {
         return shouldShowDialog;
      }

      public void show() {
      }

      public int getExitCode() {
         return expectedExitCode;
      }

      public void setShowScrField(boolean show) {
         methodsCalledLogString.append("setShowScrField("+ show+")");
      }
   }

   public void testShowDialog_ShowScr() throws VcsException
   {
      TransparentConfiguration configuration = new TransparentConfiguration();
      configuration.scrTextFileName = "something";
      project.addComponent(configuration);
      transparentVcs.initComponent();
      handler = new CheckInHandler(transparentVcs);
      handler.setCheckInDialog(new TestCheckInProjectDialog(project));
      shouldShowDialog = true;
      handler.showDialog();
      assertMethodsCalled("setShowScrField(true)");
   }

   public void testShowDialog_HideScr() throws VcsException
   {
      TransparentConfiguration configuration = new TransparentConfiguration();
      configuration.scrTextFileName = "";
      project.addComponent(configuration);
      transparentVcs.initComponent();
      handler = new CheckInHandler(transparentVcs);
      handler.setCheckInDialog(new TestCheckInProjectDialog(project));
      shouldShowDialog = true;
      handler.showDialog();
      assertMethodsCalled("setShowScrField(false)");
   }

   private void assertMethodsCalled(String expectedMethodsCalledLog)
   {
      assertEquals("method called", expectedMethodsCalledLog, methodsCalledLogString.toString());
   }


}
