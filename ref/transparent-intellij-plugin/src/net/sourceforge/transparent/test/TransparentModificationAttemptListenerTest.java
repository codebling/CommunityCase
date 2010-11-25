package net.sourceforge.transparent.test;

/**
 * User: sg426575
 * Date: Oct 2, 2003
 * Time: 8:15:27 PM
 */

import com.intellij.openapi.vfs.ModificationAttemptEvent;
import com.intellij.openapi.vfs.VirtualFile;
import junit.framework.TestCase;
import net.sourceforge.transparent.CheckOutHelper;
import net.sourceforge.transparent.TransparentConfiguration;
import net.sourceforge.transparent.TransparentModificationAttemptListener;
import net.sourceforge.transparent.TransparentVcs;
import org.intellij.openapi.testing.MockProject;
import org.intellij.openapi.testing.MockVirtualFile;
import org.intellij.openapi.testing.MockVirtualFileManager;
import org.intellij.plugins.util.RegularFileFilter;
import org.intellij.plugins.util.testing.CallLog;

import javax.swing.*;

public class TransparentModificationAttemptListenerTest extends TestCase {
   TransparentModificationAttemptListener listener;
   private CallLog callLog;
   private TransparentConfiguration config;
   private int askUserResult;
   public static final MockVirtualFile FILE1 = new MockVirtualFile("good1", false);
   public static final MockVirtualFile FILE2 = new MockVirtualFile("good2", false);
   public static final MockVirtualFile BAD_FILE = new MockVirtualFile("bad", false);

   public void setUp() throws Exception {
      callLog = new CallLog();
      MockProject project = new MockProject();
      config = new TransparentConfiguration(project);
      project.addComponent(config);
      TransparentVcs vcs = new TransparentVcs(project);
      vcs.initTransparentConfiguration();
      CheckOutHelper checkOutHelper = new CheckOutHelper(null, null, null, null) {
         public void checkOutOrHijackFile(VirtualFile file) {
            callLog.addActualCallTo("checkOut "+file.getName());
         }

         public boolean shouldHijackFile(VirtualFile file) {
            return true;
         }
      };
      RegularFileFilter filter = new RegularFileFilter() {
         public boolean accept(VirtualFile file) {
            return file != BAD_FILE;
         }
      };
      listener = new TransparentModificationAttemptListener(vcs, checkOutHelper, filter) {
         protected int askUser(String message) {
            return askUserResult;
         }
      };
   }

   public void testreadOnlyModificationAttempt_NoFiles() throws Exception {
      ModificationAttemptEvent event = createEvent(new VirtualFile[0]);
      listener.readOnlyModificationAttempt(event);
      callLog.verifyNoCalls();
      assertTrue(event.isConsumed());
   }

   public void testreadOnlyModificationAttempt_EventConsumed() throws Exception {
      ModificationAttemptEvent event = createEvent(new VirtualFile[0]);
         event.consume();
         listener.readOnlyModificationAttempt(event);
         callLog.verifyNoCalls();
      }

      public void testreadOnlyModificationAttempt_FileNotUnderVcs() throws Exception {
         ModificationAttemptEvent event = createEvent(new VirtualFile[] { BAD_FILE});
         listener.readOnlyModificationAttempt(event);
         callLog.verifyNoCalls();
   }

   public void testreadOnlyModificationAttempt_UserSaysNo() throws Exception {
      config.automaticCheckout = false;
      askUserResult = JOptionPane.NO_OPTION;
      ModificationAttemptEvent event = createEvent(new VirtualFile[]{FILE1});
      listener.readOnlyModificationAttempt(event);
      callLog.verifyNoCalls();
   }

   public void testreadOnlyModificationAttempt_AutomaticCheckoutOn() throws Exception {
      config.automaticCheckout = true;
      askUserResult = JOptionPane.NO_OPTION;
      ModificationAttemptEvent event = createEvent(new VirtualFile[]{FILE1});
      listener.readOnlyModificationAttempt(event);
      callLog.verify(new String[] {"checkOut "+FILE1.getName()});
   }

   public void testreadOnlyModificationAttempt_UserSaysYes() throws Exception {
      config.automaticCheckout = false;
      askUserResult = JOptionPane.YES_OPTION;
      ModificationAttemptEvent event = createEvent(new VirtualFile[]{FILE1});
      listener.readOnlyModificationAttempt(event);
      callLog.verify(new String[] {"checkOut "+FILE1.getName()});
   }

   public void testreadOnlyModificationAttempt_UserSaysYes_MultipleFiles() throws Exception {
      config.automaticCheckout = false;
      askUserResult = JOptionPane.YES_OPTION;
      ModificationAttemptEvent event = createEvent(new VirtualFile[]{FILE1, FILE2});
      listener.readOnlyModificationAttempt(event);
      callLog.verify(new String[] {"checkOut "+FILE1.getName(), "checkOut "+FILE2.getName()});
   }

   private ModificationAttemptEvent createEvent(VirtualFile[] virtualFiles) {
      return new ModificationAttemptEvent(new MockVirtualFileManager(), virtualFiles);
   }
}