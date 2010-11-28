package net.sourceforge.transparent;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

// TODO: Should have a ClearCaseVirtualFile that holds a VirtualFile => change the AsynchronousAction hierarchy to pass a CCVF instead

public class ClearCaseFile {
   private ClearCase _clearCase;
   private File _file;
   private Status _status;
   private ClearCaseFile _parent;

   public ClearCaseFile(File file, ClearCase clearCase) {
      _clearCase = clearCase;
      _file = file;
      updateStatus();
   }

   public ClearCaseFile(VirtualFile file, ClearCase clearCase) {
      this(new File(file.getPath()), clearCase);
   }

   public ClearCaseFile(VirtualFile parent, String name, ClearCase clearCase) {
      this(new File(parent.getPath(), name), clearCase);
   }

   // file operations

   public File getFile() {
      return _file;
   }

   public String getName() {
      return _file.getName();
   }

   public String getPath() {
      return _file.getPath();
   }

   public ClearCaseFile getParent() {
      if (_parent == null && _file.getParentFile() != null) {
         _parent = new ClearCaseFile(_file.getParentFile(), _clearCase);
      }
      return _parent;
   }

   public boolean exists() {
      return _file.exists();
   }

   public String toString() {
      return _file.getPath();
   }

   // status operations

   public boolean isCheckedOut() {
      return _status == Status.CHECKED_OUT;
   }

   public boolean isCheckedIn() {
      return _status == Status.CHECKED_IN;
   }

   public boolean isElement() {
      return _status != Status.NOT_AN_ELEMENT;
   }

   public boolean isHijacked() {
      return _status == Status.HIJACKED;
   }

   private void updateStatus() {
      _status = _clearCase.getStatus(_file);
   }

   private void assertIsElement() {
      if (!isElement())
         throw new ClearCaseException(_file.getAbsoluteFile() + " is not an element");
   }

   private void assertIsCheckedOut() {
      assertIsElement();
      if (!isCheckedOut())
         throw new ClearCaseException(_file.getAbsoluteFile() + " is not checked out");
   }

   public void undoCheckOut() {
      assertIsCheckedOut();
      _clearCase.undoCheckOut(_file);
      updateStatus();
   }

   public void checkIn(String comment, boolean useHijacked) {
      if (isHijacked() && useHijacked) {
         checkOut(false, true);
      }
      checkIn(comment);
   }

   public void checkIn(String comment) {
      assertIsElement();
      if (isCheckedOut()) {
         _clearCase.checkIn(_file, comment);
         updateStatus();
      }
   }

   public void checkOut(boolean isReserved, boolean useHijacked) {
      assertIsElement();
      if (!isCheckedOut()) {
         File newFile = null;
         if (useHijacked) {
            newFile = new File(_file.getParentFile().getAbsolutePath(), _file.getName() + ".hijacked");
            _file.renameTo(newFile);
         }

         _clearCase.checkOut(_file, isReserved);

         if (newFile != null) {
            _file.delete();
            newFile.renameTo(_file);
         }

         updateStatus();
      }
   }

   public void add(String comment) {
      if (getParent() == null) {return;}
      if (comment.equals("")) {comment = "Added " + _file.getName();}
      ensureParentIsElement(comment);

      getParent().checkOut(false, false);
      _clearCase.add(_file, comment);
      _clearCase.checkIn(_file, comment);
      getParent().checkIn(comment);

      updateStatus();
   }

   public void delete(String comment) {
      if (isCheckedOut()) undoCheckOut();
      if (isParentDeleted()) {return;}
      getParent().checkOut(false, false);

      if (comment.equals("")) {comment = "Deleted " + _file.getName();}
      _clearCase.delete(_file, comment);

      getParent().checkIn(comment);

      updateStatus();
   }

   private boolean isParentDeleted() {
      return !getParent().getFile().exists();
   }

   public void move(ClearCaseFile target, String comment) {
      ensureParentIsElement(comment);
      getParent().checkOut(false, false);
      target.getParent().checkOut(false, false);

      if (comment.equals("")) {comment = "Moved " + _file.getPath() + " to " + target.getFile().getPath();}
      _clearCase.move(_file, target.getFile(), comment);

      getParent().checkIn(comment);
      target.getParent().checkIn(comment);

      updateStatus();
      target.updateStatus();
   }

   public void rename(String newName, String comment) {
      ensureParentIsElement(comment);
      getParent().checkOut(false, false);
      if (comment.equals("")) {comment = "Renamed " + _file.getName() + " to " + newName;}
      _clearCase.move(_file, new File(_file.getParentFile(), newName), comment);
      getParent().checkIn(comment);

      updateStatus();
   }

   private void ensureParentIsElement(String comment) {
      if (!getParent().isElement()) {getParent().add(comment);}
      if (!getParent().isElement()) {throw new ClearCaseException("Could not add " + _file.getAbsoluteFile());}
   }

}
