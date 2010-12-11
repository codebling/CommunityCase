/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.config;

import org.jetbrains.annotations.NonNls;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * The version of the git. Note that the version number ignores build and commit hash.
 */
public final class Version implements Comparable<Version> {
  /**
   * The format of the "git version" (four components)
   */
  @NonNls private static final MessageFormat FORMAT =
    new MessageFormat("{0}cleartool                         {1,number,integer}.{2,number,integer}.{3,number,integer}.{4,number,integer}{5}", Locale.US);
            //"git version {0,number,integer}.{1,number,integer}.{2,number,integer}.{3,number,integer}", Locale.US);
  /**
   * Invalid version number
   */
  public static final Version INVALID = new Version(0, 0, 0, 0);
  /**
   * The minimal supported version
   */
  public static final Version MIN = new Version(1, 6, 0, 0);

  /**
   * Major version number
   */
  private final int myMajor;
  /**
   * Minor version number
   */
  private final int myMinor;
  /**
   * Revision number
   */
  private final int myRevision;
  /**
   * Patch level
   */
  private final int myPatchLevel;

  /**
   * A constructor from fields
   *
   * @param major      a major number
   * @param minor      a minor number
   * @param revision   a revision
   * @param patchLevel a patch level
   */
  public Version(int major, int minor, int revision, int patchLevel) {
    myMajor = major;
    myMinor = minor;
    myRevision = revision;
    myPatchLevel = patchLevel;
  }

  /**
   * Parse output of version command
   *
   * @param version the output of the vcs executable's version command
   * @return the version of the vcs executable tool
   */
  public static Version parse(String version) {
    try {
      Object[] parsed = FORMAT.parse(version);
      int major = ((Long)parsed[1]).intValue();
      int minor = ((Long)parsed[2]).intValue();
      int revision = ((Long)parsed[3]).intValue();
      int patchLevel = ((Long)parsed[4]).intValue();
      return new Version(major, minor, revision, patchLevel);
    }
    catch (ParseException ex) {
      throw new IllegalArgumentException("Unsupported format of version command output: " + version);
    }
  }

  /**
   * @return true if the version is supported by the plugin
   */
  public boolean isSupported() {
    return compareTo(MIN) >= 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Version && compareTo((Version)obj) == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return ((myMajor * 17 + myMinor) * 17 + myRevision) * 17 + myPatchLevel;
  }

  /**
   * {@inheritDoc}
   */
  public int compareTo(final Version o) {
    int d = myMajor - o.myMajor;
    if (d != 0) {
      return d;
    }
    d = myMinor - o.myMinor;
    if (d != 0) {
      return d;
    }
    d = myRevision - o.myRevision;
    if (d != 0) {
      return d;
    }
    return myPatchLevel - o.myPatchLevel;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    //noinspection ConcatenationWithEmptyString
    return "" + myMajor + "." + myMinor + "." + myRevision + "." + myPatchLevel;
  }

  /**
   * Compare version number
   *
   * @param gitVersion a git revision to compare with
   * @return the comparison result
   */
  public boolean isLessOrEqual(final Version gitVersion) {
    return gitVersion != null && compareTo(gitVersion) <= 0;
  }
}
