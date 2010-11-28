package net.sourceforge.transparent;

import net.sourceforge.eclipseccase.jni.Clearcase;

import java.io.File;

public class NativeClearCase implements ClearCase {
    Clearcase clearcase;

    public String getName() {
        return NativeClearCase.class.getName();
    }

    public NativeClearCase() {
        Clearcase.isElement("c:/");
    }

    private void checkStatus(Clearcase.Status status) {
        if (!status.status) {
            throw new ClearCaseException(status.message);
        }
    }

    public void undoCheckOut(File file) {
        checkStatus(Clearcase.uncheckout(file.getPath(), false));
    }

    public void checkIn(File file, String comment) {
        checkStatus(Clearcase.checkin(file.getPath(), comment, true));
    }

    public void checkOut(File file, boolean isReserved) {
        checkStatus(Clearcase.checkout(file.getPath(), "", isReserved, true));
    }

    /**
     * delete the file in clearcase but not in filesystem
     */
    public void delete(File file, String comment) {
        checkStatus(Clearcase.delete(file.getPath(), comment));
    }

    /**
     * add an existing file to filesystem
     */
    public void add(File file, String comment) {
        checkStatus(Clearcase.add(file.getPath(), comment, file.isDirectory()));
    }

   public void move(File file, File target, String comment) {
       checkStatus(Clearcase.move(file.getPath(), target.getPath(), comment));
   }

    public Status getStatus(File file) {
        if (!isElement(file)) {
            return Status.NOT_AN_ELEMENT;
        } else if (isCheckedOut(file)) {
            return Status.CHECKED_OUT;
        } else if (Clearcase.isHijacked(file.getPath())) {
            return Status.HIJACKED;
        } else {
            return Status.CHECKED_IN;
        }
    }

    public boolean isElement(File file) {
        return Clearcase.isElement(file.getPath());
    }

    public boolean isCheckedOut(File file) {
        return Clearcase.isCheckedOut(file.getPath());
    }

    public void cleartool(String cmd) {
        checkStatus(Clearcase.cleartool(cmd));
    }

    public CheckedOutStatus getCheckedOutStatus(File file) {
        Clearcase.Status status = Clearcase.cleartool("lscheckout -fmt %Rf -directory " + file.getPath());

        if ((status == null) || (status.message == null))
            return CheckedOutStatus.NOT_CHECKED_OUT;
        else if (status.message.equalsIgnoreCase("reserved"))
            return CheckedOutStatus.RESERVED;
        else if (status.message.equalsIgnoreCase("unreserved"))
            return CheckedOutStatus.UNRESERVED;
        else
            return CheckedOutStatus.NOT_CHECKED_OUT;
    }

}