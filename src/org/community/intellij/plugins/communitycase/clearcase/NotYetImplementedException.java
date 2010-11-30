package org.community.intellij.plugins.communitycase.clearcase;

//TODO:  * Eliminate all uses of this class by implementing the methods!!!
/**
 * Marker Exception for methods which have not yet been implemented.
 * Eliminate all uses of this class by implementing the methods!!!
 *
 * @author worsecodes
 */
public class NotYetImplementedException extends ClearCaseException {
  public NotYetImplementedException() {
    super("Eek! Not yet implemented!!! Fix me pls.");
  }
}
