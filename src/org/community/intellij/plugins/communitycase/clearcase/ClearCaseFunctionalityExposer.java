package org.community.intellij.plugins.communitycase.clearcase;

import net.sourceforge.eclipseccase.jni.Clearcase;

import java.io.File;
import java.nio.channels.NotYetBoundException;

/**
 * Abrstracts access to ClearCase operations.
 *
 * Abstracting access to ClearCase funtionality allows us some flexibility in choosing how
 * to implement access to ClearCase.

 * @author worsecodes
 */
public class ClearCaseFunctionalityExposer {

  /**
   * Adds the given file to clearcase source control.  This requires
   * the parent directory to be under version control and checked
   * out.
   *
   * @param file the file to add to version control
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void add(File file) throws ClearCaseException {
    add(file, "");
  }

  /**
   * Adds the given file to clearcase source control.  This requires
   * the parent directory to be under version control and checked
   * out.
   *
   * @param file the file to add to version control
   * @param comment the comment to apply to the added file
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void add(File file, String comment) throws ClearCaseException {
    add(file, comment, isLeaveCheckedOutAfterAdd());
  }

  /**
   * Adds the given file to clearcase source control.  This requires
   * the parent directory to be under version control and checked
   * out.
   *
   * Set <code>leaveCheckedOutAfterAdd</code> to false to check in the file immediately
   * after creating the \0 element and create a \1 element.
   *
   * Set <code>leaveCheckedOutAfterAdd</code> to true to check out the \0 version of the element.
   *
   * @param file the file to add to version control
   * @param comment the comment to apply to the added file
   * @param leaveCheckedOutAfterAdd whether the file should be in the checked out state after the add or not
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void add(File file, String comment, boolean leaveCheckedOutAfterAdd) throws ClearCaseException {
    throw new NotYetImplementedException();

    //how do we control whether it is checked out after it is added?
    //does add check in the parent directory?
    //does the comment "Added file element \"file\"." get added in addition to comment?

    //check if parent directory is under version control and checked out
    //add file
/*
    Clearcase.Status result = Clearcase.add(file.toString(), comment, file.isDirectory());
    if(!result.status)
      throw new ClearCaseException(result.message);
*/

  }

  /**
   * Checks in <code>file</code>.
   *
   * @param file the file to check in
   * @param comment the check in comment
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void checkIn(File file, String comment) throws ClearCaseException {
    if(!isCheckedOut(file)) {
      if(!isVersionControlled(file))
        add(file, comment, isLeaveCheckedOutAfterAdd());
      else
        if(isHijacked(file))
          convertHijackToCheckOut(file);
        else //the file is not hijacked nor checked out. Check out and check in?
          checkOut(file, comment, true); //reserved true since we'll be doing a quick checkin
    }
    Clearcase.Status result = Clearcase.checkin(file.toString(), comment, isPreserveModificationTime());
    if(!result.status)
      throw new ClearCaseException(result.message);
  }

  /**
   * Checks in <code>file</code>.
   * @param file the file to check in
   * @param comment the check in comment
   * @param checkInIdentical) whether or not a file that is identical to the last version should be checked in
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void checkIn(File file, String comment, boolean checkInIdentical) throws ClearCaseException {
    throw new NotYetBoundException();
  }

  /**
   * Checks out <code>file</code>.
   * @param file the file to check out
   * @param comment the check out comment
   * @param checkOutReserved whether or not the check out should be reserved
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void checkOut(File file, String comment, boolean checkOutReserved) throws ClearCaseException {
    if(!isVersionControlled(file))
      add(file, comment, isLeaveCheckedOutAfterAdd());
    else
      if(isHijacked(file))
        convertHijackToCheckOut(file);
    Clearcase.Status result = Clearcase.checkout(file.toString(), comment, checkOutReserved, isPreserveModificationTime());
    if(!result.status)
      throw new ClearCaseException(result.message);
  }

  /**
   * Uses the contents of <code>file</code> as a new check out.
   *  
   * @param file the currently hijacked file to convert
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void convertHijackToCheckOut(File file) throws ClearCaseException {
    throw new NotYetImplementedException();
  }

  //don't think we should be using this one..
  private void execute(String command) throws ClearCaseException {
    Clearcase.Status result = Clearcase.cleartool(command);
    if (!result.status)
      throw new ClearCaseException(result.message);
  }

  /**
   * Removes the given file from clearcase source control (rmname NOT
   * rmelem).  This requires the parent directory to be under version
   * control and checked out.
   * 
   * @param file the file to delete from version control
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void delete(File file) throws ClearCaseException {
    //does delete check in the parent directory after deletion?
    //does delete add the "Uncataloged file element \"file\"." in addition to the comment we provide?

    //check if parent directory is checked out
    //delete the file
  }

  /**
   * Tests if file is currently checked out.
   *
   * @param file the file for which to retrieve checkout status
   * @return true if the file is checked out, false otherwise
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public boolean isCheckedOut(File file) throws ClearCaseException {

    //we may have to check to see if the operation was successful to see if an exception needs to be thrown
    return Clearcase.isCheckedOut(file.toString());
  }

  /**
   * Tests if file is identical to its previous version.
   *
   * @param file the file to test
   * @return true if the file is checked out and different from its previous version, false otherwise
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public boolean isIdentical(File file) throws ClearCaseException {

    //we may have to check to see if the operation was successful to see if an exception needs to be thrown
    return !Clearcase.isDifferent(file.toString());
  }

  /**
   * Tests if file is under version control.
   *
   * @param file the file to test
   * @return true if the file is version-controlled, false otherwise
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public boolean isVersionControlled(File file) throws ClearCaseException {
    //what happens if the file whose name we pass does not exist locally but is an element?

    //we may have to check to see if the operation was successful to see if an exception needs to be thrown
    return Clearcase.isElement(file.toString());
  }

  /**
   * Tests if file is hijacked.
   *
   * @param file the file to test
   * @return true if the file is modified but not checked out, false otherwise
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public boolean isHijacked(File file) throws ClearCaseException {
    //what happens if the file whose name we pass does not exist locally but is an element?

    //we may have to check to see if the operation was successful to see if an exception needs to be thrown
    return Clearcase.isHijacked(file.toString());
  }

  private boolean isSnapshot(File file) throws ClearCaseException {
    throw new NotYetImplementedException();

    //Returns true if the file is under version control and part of a snapshot view
    //public static native boolean isSnapShot(String file);
  }

  private boolean move() throws ClearCaseException {
    throw new NotYetImplementedException();

    /** Moves file to newfile.  The parent directories of both file and newfile must be checked out.  Comment can be empty string. */
    //public static native Status move(String file, String newfile, String comment);
  }

  /**
   * Restores (resets) the file to its last versioned state,
   * removing the check out currently held on it.
   *
   * @param file the file for which to undo the check out
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void undoCheckOut(File file) throws ClearCaseException {
    Clearcase.Status result = Clearcase.uncheckout(file.toString(), isKeepUndoneCheckOut());
    if (!result.status)
      throw new ClearCaseException(result.message);
  }

  /**
   * Restores (resets) the file to its last versioned state,
   * removing the hijack currently held on it.
   *
   * @param file the file for which to undo the hijack
   * @throws ClearCaseException if an exception occurs during the operation
   */
  public void undoHijack(File file) throws ClearCaseException {
    throw new NotYetImplementedException();
  }

  //*** DEFAULTS ***
  //(implement these methods, perhaps as configured values or member variables, if some control is needed)

  private boolean isLeaveCheckedOutAfterAdd() {
    return true;
  }
  private boolean isPreserveModificationTime() {
    return true;
  }
  private boolean isKeepUndoneCheckOut() {
    return true;
  }
}
