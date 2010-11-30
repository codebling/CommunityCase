package org.community.intellij.plugins.communitycase.clearcase;

/**
 * @author worsecodes
 */
public class ClearCaseException extends Exception {

  public ClearCaseException() {
    super("The operation failed");
  }

  protected ClearCaseException(String message) {
    super(message);
  }
}
