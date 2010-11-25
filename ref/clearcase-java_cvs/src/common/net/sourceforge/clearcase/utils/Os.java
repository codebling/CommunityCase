/*
 * Copyright 2001-2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *  
 */
package net.sourceforge.clearcase.utils;

import java.util.Locale;

/**
 * Condition that tests the OS type.
 * 
 * @author Stefan Bodewig
 * @author Magesh Umasankar
 * @since Ant 1.4
 * @version $Revision: 1.4 $
 */
public class Os {

	public static final String OS_400 = "os/400"; //$NON-NLS-1$

	public static final String OS_390 = "os/390"; //$NON-NLS-1$

	public static final String Z_OS = "z/os"; //$NON-NLS-1$

	public static final String WINDOWS_CE = "ce"; //$NON-NLS-1$

	public static final String WINDOWS_ME = "me"; //$NON-NLS-1$

	public static final String WINDOWS_98 = "98"; //$NON-NLS-1$

	public static final String WINDOWS_95 = "95"; //$NON-NLS-1$

	public static final String WIN9X = "win9x"; //$NON-NLS-1$

	public static final String X = "x"; //$NON-NLS-1$

	public static final String NONSTOP_KERNEL = "nonstop_kernel"; //$NON-NLS-1$

	public static final String TANDEM = "tandem"; //$NON-NLS-1$

	public static final String DOS = "dos"; //$NON-NLS-1$

	public static final String NETWARE = "netware"; //$NON-NLS-1$

	private static final String OS_NAME = System.getProperty("os.name") //$NON-NLS-1$
			.toLowerCase(Locale.US);

	private static final String OS_ARCH = System.getProperty("os.arch") //$NON-NLS-1$
			.toLowerCase(Locale.US);

	private static final String OS_VERSION = System.getProperty("os.version") //$NON-NLS-1$
			.toLowerCase(Locale.US);

	private static final String PATH_SEP = System.getProperty("path.separator"); //$NON-NLS-1$

	public static final String WINDOWS = "windows"; //$NON-NLS-1$

	public static final String MAC = "mac"; //$NON-NLS-1$

	public static final String UNIX = "unix"; //$NON-NLS-1$

	public static final String OS_2 = "os/2"; //$NON-NLS-1$

	public static final String OPENVMS = "openvms"; //$NON-NLS-1$

	/**
	 * Determines if the OS on which Ant is executing matches the given OS
	 * family.
	 * 
	 * @param family
	 *            the family to check for
	 * @return true if the OS matches
	 * @since 1.5
	 */
	public static boolean isFamily(String family) {
		return isOs(family, null, null, null);
	}

	/**
	 * Determines if the OS on which Ant is executing matches the given OS name.
	 * 
	 * @param name
	 *            the OS name to check for
	 * @return true if the OS matches
	 * @since 1.7
	 */
	public static boolean isName(String name) {
		return isOs(null, name, null, null);
	}

	/**
	 * Determines if the OS on which Ant is executing matches the given OS
	 * architecture.
	 * 
	 * @param arch
	 *            the OS architecture to check for
	 * @return true if the OS matches
	 * @since 1.7
	 */
	public static boolean isArch(String arch) {
		return isOs(null, null, arch, null);
	}

	/**
	 * Determines if the OS on which Ant is executing matches the given OS
	 * version.
	 * 
	 * @param version
	 *            the OS version to check for
	 * @return true if the OS matches
	 * @since 1.7
	 */
	public static boolean isVersion(String version) {
		return isOs(null, null, null, version);
	}

	/**
	 * Determines if the OS on which Ant is executing matches the given OS
	 * family, name, architecture and version
	 * 
	 * @param family
	 *            The OS family
	 * @param name
	 *            The OS name
	 * @param arch
	 *            The OS architecture
	 * @param version
	 *            The OS version
	 * @return true if the OS matches
	 * @since 1.7
	 */
	public static boolean isOs(String family, String name, String arch,
			String version) {
		boolean retValue = false;

		if (family != null || name != null || arch != null || version != null) {

			boolean isFamily = true;
			boolean isName = true;
			boolean isArch = true;
			boolean isVersion = true;

			if (family != null) {
				if (family.equals(WINDOWS)) {
					isFamily = OS_NAME.indexOf(WINDOWS) > -1;
				} else if (family.equals(OS_2)) {
					isFamily = OS_NAME.indexOf(OS_2) > -1;
				} else if (family.equals(NETWARE)) {
					isFamily = OS_NAME.indexOf(NETWARE) > -1;
				} else if (family.equals(DOS)) {
					isFamily = PATH_SEP.equals(";") && !isFamily(NETWARE); //$NON-NLS-1$
				} else if (family.equals(MAC)) {
					isFamily = OS_NAME.indexOf(MAC) > -1;
				} else if (family.equals(TANDEM)) {
					isFamily = OS_NAME.indexOf(NONSTOP_KERNEL) > -1;
				} else if (family.equals(UNIX)) {
					isFamily = PATH_SEP.equals(":") && !isFamily(OPENVMS) //$NON-NLS-1$
							&& (!isFamily(MAC) || OS_NAME.endsWith(X));
				} else if (family.equals(WIN9X)) {
					isFamily = isFamily(WINDOWS)
							&& (OS_NAME.indexOf(WINDOWS_95) >= 0
									|| OS_NAME.indexOf(WINDOWS_98) >= 0
									|| OS_NAME.indexOf(WINDOWS_ME) >= 0 || OS_NAME
									.indexOf(WINDOWS_CE) >= 0);
				} else if (family.equals(Z_OS)) {
					isFamily = OS_NAME.indexOf(Z_OS) > -1
							|| OS_NAME.indexOf(OS_390) > -1;
				} else if (family.equals(OS_400)) {
					isFamily = OS_NAME.indexOf(OS_400) > -1;
				} else if (family.equals(OPENVMS)) {
					isFamily = OS_NAME.indexOf(OPENVMS) > -1;
				} else
					throw new RuntimeException(
							"Don\'t know how to detect os family \"" + family //$NON-NLS-1$
									+ "\""); //$NON-NLS-1$
			}
			if (name != null) {
				isName = name.equals(OS_NAME);
			}
			if (arch != null) {
				isArch = arch.equals(OS_ARCH);
			}
			if (version != null) {
				isVersion = version.equals(OS_VERSION);
			}
			retValue = isFamily && isName && isArch && isVersion;
		}
		return retValue;
	}
}