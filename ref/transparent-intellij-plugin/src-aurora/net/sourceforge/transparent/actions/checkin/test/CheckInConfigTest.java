package net.sourceforge.transparent.actions.checkin.test;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import net.sourceforge.transparent.TransparentConfiguration;
import net.sourceforge.transparent.actions.checkin.CheckInConfig;
import net.sourceforge.transparent.actions.checkin.CheckInFileDialog;
import org.intellij.openapi.testing.MockProject;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by IntelliJ IDEA.
 * User: csheppe
 * Date: Mar 10, 2003
 * Time: 2:48:16 PM
 * To change this template use Options | File Templates.
 */
public class CheckInConfigTest extends TestCase {
    private MockProject project;
    private CheckInConfig config;

   protected void setUp() {
        project = new MockProject();
        config = new CheckInConfig(project);
    }

    public class MockTransparentConfiguration extends TransparentConfiguration {
       public MockTransparentConfiguration() {
          super(project);
       }
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

      final StringWriter stringWriter = new StringWriter();
      CheckInConfig config = new CheckInConfig(project)
      {
         protected Writer createWriter(String scrTextFileName) throws IOException
         {
            return stringWriter;
         }
      };

      config.scrTextFileName = scrFileName;
      config.lastScr = "Scr234";
      config.writeScr();

      assertEquals("Scr Number Equals", expectedScr, stringWriter.toString());
   }

   public void testCopyToDialog_ShowScr() throws VcsException
   {
      //TODO: Move scrTextFileName to CheckInConfig
      config = new CheckInConfig(project);
      config.scrTextFileName = "something";
      CheckInFileDialog fields = new CheckInFileDialog(project,new VirtualFile[0], config.getCheckInEnvironment(), 0);
      config.copyToDialog(fields);
      assertTrue("scr should show", fields.isShowScrField());
      assertEquals("src", config.lastScr, fields.getScr());
   }

   public void testShowDialog_HideScr() throws VcsException
   {
      config = new CheckInConfig(project);
      config.scrTextFileName = "";
      CheckInFileDialog fields = new CheckInFileDialog(project,new VirtualFile[0], config.getCheckInEnvironment(), 0);
      config.copyToDialog(fields);
      assertFalse("scr should not show", fields.isShowScrField());
    }
}
