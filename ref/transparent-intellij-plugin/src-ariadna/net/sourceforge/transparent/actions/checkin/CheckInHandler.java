package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import net.sourceforge.transparent.TransparentVcs;
import net.sourceforge.transparent.TransparentConfiguration;
import net.sourceforge.transparent.actions.checkin.BaseCheckInDialog;

import javax.swing.*;

public class CheckInHandler {
   private   boolean           forAllChosen;
   private   int               dialogResult;
   private   String            comment;
   private   String            scr;
   private   TransparentVcs    vcs;
   protected BaseCheckInDialog checkInDialog;

   public CheckInHandler(TransparentVcs _vcs) {
      this.vcs          = _vcs;
      this.forAllChosen = false;
      this.comment      = "";
      this.scr          = "";
   }

   public boolean askForCheckInConfirmation() throws VcsException {
      if (forAllChosen) {
         return true;
      }

      showDialog();

      if (dialogResult == BaseCheckInDialog.CANCEL_EXIT_CODE) {
         return false;
      }

      if (dialogResult == BaseCheckInDialog.OK_ALL_EXIT_CODE) {
         // Do this only after success.
         forAllChosen = true;
      }

      setComment(checkInDialog.getCommentArea().getText());
      setScr(checkInDialog.getScrField().getText());
      writeScr();

      return true;
   }

   public void showDialog() throws VcsException {
      runOnAWTThread(new Runnable() {
         public void run() {
            if (checkInDialog.shouldShowDialog()) {
               checkInDialog.setComment(comment);
               checkInDialog.setScr(scr);
               checkInDialog.setShowScrField(isScrNeeded());
               checkInDialog.show();
               dialogResult = checkInDialog.getExitCode();
            } else
            dialogResult = BaseCheckInDialog.CANCEL_EXIT_CODE;
         }
      });
   }

   public boolean isScrNeeded() {
      return vcs.getTransparentConfiguration().scrTextFileName.length() != 0;
   }

   private void runOnAWTThread(Runnable runnable) throws VcsException {
      if (EventQueue.isDispatchThread()) {
         runnable.run();
      } else {
         try {
            EventQueue.invokeAndWait(runnable);
         } catch (Exception x) {
            x.printStackTrace();
            throw new VcsException(x);
         }
      }
   }

   public int getDialogResult() {
      return dialogResult;
   }

   public String getScr() {
      return scr;
   }

   public void setScr(String scr) {
      this.scr = scr;
   }

   public String getComment() {
      return comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public void writeScr() throws VcsException {
      if (!isScrNeeded())
         return;

      Writer writer = null;
      try {
         writer = createWriter(vcs.getTransparentConfiguration().scrTextFileName);
         writer.write(getScr());
      } catch (Exception e) {
         e.printStackTrace();  //To change body of catch statement use Options | File Templates.
         throw new VcsException("Could not write to the scr file: " + e.getMessage());
      } finally {
         try {
            if (writer != null)
               writer.close();
         } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
         }
      }
   }

   protected Writer createWriter(String scrTextFileName) throws IOException {
      FileWriter fileWriter = new FileWriter(scrTextFileName);
      return fileWriter;
   }

   public void setDialogResult(int dialogResult) {
      this.dialogResult = dialogResult;
   }

   public boolean isForAllChosen() {
      return forAllChosen;
   }

   public void setForAllChosen(boolean forAllChosen) {
      this.forAllChosen = forAllChosen;
   }

   public BaseCheckInDialog getCheckInDialog() {
      return checkInDialog;
   }

   public void setCheckInDialog(BaseCheckInDialog checkInDialog) {
      this.checkInDialog = checkInDialog;
   }
}
