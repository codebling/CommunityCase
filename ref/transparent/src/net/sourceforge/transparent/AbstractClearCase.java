package net.sourceforge.transparent;

import net.sourceforge.clearcase.simple.IClearcase;
import net.sourceforge.clearcase.simple.ClearcaseFactory;
import net.sourceforge.clearcase.simple.ClearcaseException;

import java.io.File;

/**
 * User: sg426575
 * Date: Aug 28, 2003
 * Time: 12:45:45 PM
 */
public class AbstractClearCase implements ClearCase {
   IClearcase cc;

   public AbstractClearCase(int type) throws ClearcaseException {
      cc = ClearcaseFactory.getInstance().createInstance(type);
   }

   public String getName() {
      return NewNativeClearCase.class.getName();
   }

   public void move(File file, File target, String comment) {
      checkStatus(cc.move(file.getPath(), target.getPath(), comment));
   }

   // TODO: for ClearcaseJNI remove the jacob leading error message
   private void checkStatus(IClearcase.Status status) {
      if (!status.status) {
         throw new ClearCaseException(status.message);
      }
   }

   public void undoCheckOut(File file) {
      checkStatus(cc.uncheckout(file.getPath(), false));
   }

   public void checkIn(File file, String comment) {
      checkStatus(cc.checkin(file.getPath(), comment, true));
   }

   public void checkOut(File file, boolean isReserved) {
      checkStatus(cc.checkout(file.getPath(), "", isReserved, true));
   }

   /**
    * delete the file in clearcase but not in filesystem
    */
   public void delete(File file, String comment) {
      checkStatus(cc.delete(file.getPath(), ""));
   }

   /**
    * add an existing file to filesystem
    */
   public void add(File file, String comment) {
      checkStatus(cc.add(file.getPath(), "", file.isDirectory()));
   }

   public Status getStatus(File file) {
      if (isHijacked(file)) {
         return Status.HIJACKED;
      } else if (!isElement(file)) {
         return Status.NOT_AN_ELEMENT;
      } else if (isCheckedOut(file)) {
         return Status.CHECKED_OUT;
      } else {
         return Status.CHECKED_IN;
      }
   }

   public boolean isHijacked(File file) {
      return cc.isHijacked(file.getPath());
   }

   public boolean isElement(File file) {
      return cc.isElement(file.getPath());
   }

   public boolean isCheckedOut(File file) {
      return cc.isCheckedOut(file.getPath());
   }

   public void cleartool(String cmd) {
      checkStatus(cc.cleartool(cmd));
   }

   public CheckedOutStatus getCheckedOutStatus(File file) {
      IClearcase.Status status = cc.cleartool("lscheckout -fmt %Rf -directory " + file.getPath());

      if ((status == null) || (status.message == null)) {
         return CheckedOutStatus.NOT_CHECKED_OUT;
      } else if (status.message.equalsIgnoreCase("reserved")) {
         return CheckedOutStatus.RESERVED;
      } else if (status.message.equalsIgnoreCase("unreserved")) {
         return CheckedOutStatus.UNRESERVED;
      } else {
         return CheckedOutStatus.NOT_CHECKED_OUT;
      }
   }
}
