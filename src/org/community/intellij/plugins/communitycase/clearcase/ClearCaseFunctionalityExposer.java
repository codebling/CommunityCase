package org.community.intellij.plugins.communitycase.clearcase;

import net.sourceforge.eclipseccase.jni.Clearcase;

import java.io.File;

/**
 * Abrstracts access to ClearCase operations.
 *
 * Abstracting access to ClearCase funtionality allows us some flexibility in choosing how
 * to implement access to ClearCase.
 */
public class ClearCaseFunctionalityExposer {

  public boolean isCheckedOut(File file) {
    return Clearcase.isCheckedOut(file.toString());
  }
}
