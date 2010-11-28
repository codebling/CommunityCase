package net.sourceforge.transparent;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;

public class ClearCaseDecorator implements ClearCase {
   public static final Logger LOG = Logger.getInstance("net.sourceforge.transparent.ClearCase");

   ClearCase clearCase;

   public ClearCaseDecorator(ClearCase clearcase) {
      this.clearCase = clearcase;
   }

   public String getName() {
      return clearCase.getName();
   }

   public ClearCase getClearCase() {
      return clearCase;
   }

   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ClearCaseDecorator)) return false;

      final ClearCaseDecorator clearCaseDecorator = (ClearCaseDecorator) o;

      if (!clearCase.equals(clearCaseDecorator.clearCase)) return false;

      return true;
   }

   public int hashCode() {
      return clearCase.hashCode();
   }

   public void move(File file, File target, String comment) {
      debug("move of " + file.getPath() + " to " + target.getPath());
      clearCase.move(file, target, comment);
   }

   public void undoCheckOut(File file) {
      debug("uncheckout of " + file.getPath());
      clearCase.undoCheckOut(file);
   }

   public void checkIn(File file, String comment) {
      debug("checkin of " + file.getPath());
      clearCase.checkIn(file, comment);
   }

   public void checkOut(File file, boolean isReserved) {
      debug("checkout of " + file.getPath());
      clearCase.checkOut(file, isReserved);
   }

   /**
    * delete the file in clearcase but not in filesystem
    */
   public void delete(File file, String comment) {
      debug("delete of " + file.getPath());
      clearCase.delete(file, comment);
   }

   /**
    * add an existing file to filesystem
    */
   public void add(File file, String comment) {
      debug("add of " + file);
      clearCase.add(file, comment);
   }

   public Status getStatus(File file) {
      Status status = clearCase.getStatus(file);
      debug("status of " + file + "=" + status);
      return status;
   }

   public boolean isElement(File file) {
      return clearCase.isElement(file);
   }

   public boolean isCheckedOut(File file) {
      return clearCase.isCheckedOut(file);
   }

   public void cleartool(String cmd) {
      debug("executing cleartool " + cmd);
      clearCase.cleartool(cmd);
   }

    public CheckedOutStatus getCheckedOutStatus(File file) {
        return clearCase.getCheckedOutStatus(file);
    }

    static public void debug(String message) {
      if (LOG.isDebugEnabled())
         LOG.debug(message);
   }
}