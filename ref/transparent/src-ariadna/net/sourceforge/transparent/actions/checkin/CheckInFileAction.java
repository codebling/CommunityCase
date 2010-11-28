package net.sourceforge.transparent.actions.checkin;

import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.ui.Messages;
import org.intellij.openapi.OpenApiFacade;
import net.sourceforge.transparent.Status;
import net.sourceforge.transparent.actions.SynchronousAction;
import net.sourceforge.transparent.actions.ActionContext;

import java.io.*;

public class CheckInFileAction extends SynchronousAction {
    static CheckInHandler handler;
    private CheckInFileDialog checkInDialog;

    protected boolean isEnabled(VirtualFile file, ActionContext context) throws VcsException {
        return OpenApiFacade.getFileStatusManager(context.project).getStatus(file) == FileStatus.MODIFIED;
    }

    protected void perform(VirtualFile file, ActionContext context)
            throws VcsException {

        Status fileStatus = context.vcs.getFileStatus(file);

        if (fileStatus == Status.HIJACKED)
            throw new VcsException("Check Out : File is hijacked.  Please check file out before checking in.");
        else if (!(OpenApiFacade.getFileStatusManager(context.project).getStatus(file) == FileStatus.MODIFIED))
            throw new VcsException("Check In : Nothing was found to Check In.");

        checkInDialog = new CheckInFileDialog(context.project);
        checkInDialog.setFileName(file.getPresentableUrl());
        getHandler(context).setCheckInDialog(checkInDialog);
        if (getHandler(context).askForCheckInConfirmation()) {
            context.vcsHelper.doCheckinFiles(new VirtualFile[]{file}, getHandler(context).getComment());
        } else {
            isCancelled = true;
        }
    }

    protected CheckInHandler getHandler(ActionContext context) {
        if (handler == null)
            handler = new CheckInHandler(context.vcs);
        return handler;
    }

    protected void resetTransactionIndicators(ActionContext context) {
        getHandler(context).setForAllChosen(false);
    }

    protected String getActionName(ActionContext context) {
        return "Checking In File";
    }

}