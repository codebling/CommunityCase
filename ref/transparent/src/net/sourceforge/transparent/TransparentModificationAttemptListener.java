package net.sourceforge.transparent;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.EditFileProvider;
import com.intellij.openapi.vfs.ModificationAttemptEvent;
import com.intellij.openapi.vfs.ModificationAttemptListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.intellij.openapi.OpenApiFacade;
import org.intellij.plugins.util.RegularFileFilter;

import javax.swing.*;

/**
 * User: sg426575
 * Date: Oct 2, 2003
 * Time: 1:23:52 PM
 */
public class TransparentModificationAttemptListener implements ModificationAttemptListener, EditFileProvider {
   private TransparentVcs transparentVcs;
   private CheckOutHelper checkOutHelper;
   private RegularFileFilter regularFileFilter;

   public TransparentModificationAttemptListener(TransparentVcs transparentVcs, CheckOutHelper checkOutHelper, RegularFileFilter regularFileFilter) {
      this.transparentVcs = transparentVcs;
      this.checkOutHelper = checkOutHelper;
      this.regularFileFilter = regularFileFilter;
   }

   public TransparentModificationAttemptListener(TransparentVcs vcs) {
      transparentVcs = vcs;
      regularFileFilter = new RegularFileFilter();
   }

   public CheckOutHelper getCheckOutHelper() {
      // Required because the TransparentVcs.transparentConfig gets initialized in projectOpened()
      // and this object gets created in the start() which is called after => lazy initialization required
      if (checkOutHelper == null) {
         checkOutHelper = new CheckOutHelper(transparentVcs);
      }
      return checkOutHelper;
   }

   public void readOnlyModificationAttempt(ModificationAttemptEvent event) {
      if (event.isConsumed()) return;

      for (int i = 0; i < event.getFiles().length; i++) {
         VirtualFile file = event.getFiles()[i];
         if (isUnderVcs(file) && shouldCheckoutFile(file)) {
            checkOutOrHijackFile(file);
         }
      }

      event.consume();
   }

   public boolean isUnderVcs(VirtualFile file) {
      return transparentVcs.getFileFilter().accept(file) &&
             regularFileFilter.accept(file);
   }

   private void checkOutOrHijackFile(VirtualFile file) {
      getCheckOutHelper().checkOutOrHijackFile(file);
   }

   private boolean shouldCheckoutFile(VirtualFile file) {
      String path = file.getPresentableUrl();
      boolean shouldCheckoutFile = transparentVcs.getTransparentConfig().automaticCheckout;
      if (!shouldCheckoutFile) {
         String message = "The file " +
                          path +
                          " is readonly\n" +
                          "Do you want to " + (getCheckOutHelper().shouldHijackFile(file) ?
                                               "hijack it" :
                                               "check it out") + "?";
         int answer = askUser(message);
         shouldCheckoutFile = answer == JOptionPane.YES_OPTION;
      }
      return shouldCheckoutFile;
   }

   protected int askUser(final String message) {
      return JOptionPane.showConfirmDialog(null,
                                           message,
                                           "Readonly file",
                                           JOptionPane.YES_NO_OPTION,
                                           JOptionPane.QUESTION_MESSAGE,
                                           Messages.getQuestionIcon());
//      return Messages.showYesNoDialog(transparentVcs.getProject(),
//                                      message,
//                                      "Readonly file",
//                                      Messages.getQuestionIcon());
   }

   public void start() {
      OpenApiFacade.getVirtualFileManager().addModificationAttemptListener(this);
   }

   public void stop() {
      OpenApiFacade.getVirtualFileManager().removeModificationAttemptListener(this);
   }

   public void editFiles(VirtualFile[] files) {

   }

   public String getRequestText() {
      return null;
   }

}
