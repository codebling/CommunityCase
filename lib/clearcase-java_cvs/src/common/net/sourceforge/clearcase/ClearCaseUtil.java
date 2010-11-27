/*******************************************************************************
 * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net team and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Conway - initial API and implementation
 *     Eclipse.org - concepts and ideas from Eclipse
 *     Gunnar Wagenknecht - API rework, feature enhancements, bug fixes
 *******************************************************************************/
package net.sourceforge.clearcase;

/**
 * This class provides utility methods when working with the ClearCase Java API.
 * <p>
 * This class cannot be instantiated; all functionality is provided by static
 * methods.
 * </p>
 */
public class ClearCaseUtil {

	/**
	 * Quotes the specified string.
	 * <p>
	 * Appends double quotes around the string and replaces all double quotes
	 * inside the string with two single quotes.
	 * </p>
	 * 
	 * @param str
	 * @return the quoted string.
	 */
	public static String quote(String str) {
		StringBuffer result = new StringBuffer(str.length() + 10);
		result.append('"');

		// replace all double quotes with two single quotes
		for (int i = 0; i < str.length(); ++i) {
			char c = str.charAt(i);
			switch (c) {
			case '"':
				result.append("''"); //$NON-NLS-1$
				break;

			default:
				result.append(c);
				break;
			}
		}
		result.append('"');
		return result.toString();
	}

	/**
	 * Indicates if the specified string contains newline characters.
	 * 
	 * @param str
	 * @return <code>true</code> if the specified string contains newline
	 *         characters, <code>false</code> otherwise.
	 */
	public static boolean isMultiLine(String str) {
		return str.indexOf("\n") != -1 || str.indexOf("\r") != -1; //$NON-NLS-1$ //$NON-NLS-2$
	}

	// private static void appendEscapedChar(StringBuffer buffer, char c)
	// {
	// String replacement = getReplacement(c);
	// if (replacement != null)
	// {
	// buffer.append('&');
	// buffer.append(replacement);
	// buffer.append(';');
	// }
	// else
	// {
	// buffer.append(c);
	// }
	// }
	//
	// /**
	// * Returns an escaped string that can be safely wrapped into
	// * double quotes (<code>&quot;</code>) and used on the command line.
	// *
	// * <p>The replacement is similar to XML encoding:
	// * <ul>
	// * <li><code>&lt;</code> into <code>&amp;;</code></li>
	// * <li><code>&gt;</code> into <code>&amp;gt;</code></li>
	// * <li><code>&quot;</code> into <code>&amp;quot;</code></li>
	// * <li><code>'</code> into <code>&amp;apos;</code></li>
	// * <li><code>&amp;</code> into <code>&amp;amp;</code></li>
	// * </ul>
	// * </p>
	// * @param s
	// * @return
	// */
	// public static String getEscaped(String s)
	// {
	// StringBuffer result = new StringBuffer(s.length() + 10);
	// for (int i = 0; i < s.length(); ++i)
	// appendEscapedChar(result, s.charAt(i));
	// return result.toString();
	// }
	//
	// private static String getReplacement(char c)
	// {
	// // Encode special XML characters into the equivalent character
	// references.
	// // These five are defined by default for all XML documents.
	// switch (c)
	// {
	// case '<' :
	// return "lt"; //$NON-NLS-1$
	// case '>' :
	// return "gt"; //$NON-NLS-1$
	// case '"' :
	// return "quot"; //$NON-NLS-1$
	// case '\'' :
	// return "apos"; //$NON-NLS-1$
	// case '&' :
	// return "amp"; //$NON-NLS-1$
	// }
	// return null;
	// }
	//
	// public static List findCheckins(
	// ClearCaseInterface ccase,
	// String path,
	// String limitDate,
	// String username)
	// throws ClearCaseException
	// {
	// List resultList = new ArrayList();
	//
	// if (limitDate == null || limitDate.trim().length() == 0)
	// limitDate = "today";
	//
	// String userbool = "";
	// if (username != null && username.trim().length() > 0)
	// userbool = "&&created_by(" + username + ")";
	//
	// ClearCaseInterface.Status result =
	// ccase.cleartool(
	// "find "
	// + quote(path)
	// + " -cview -version '{created_since("
	// + limitDate
	// + ")"
	// + userbool
	// + "}' -print");
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// StringTokenizer st = new StringTokenizer(result.message, "\r\n");
	// while (st.hasMoreTokens())
	// {
	// String item = st.nextToken();
	// if (!item.endsWith("CHECKEDOUT"))
	// {
	// int idx = item.indexOf("@@");
	// if (idx > -1)
	// {
	// item = item.substring(0, idx);
	// }
	// resultList.add(item);
	// }
	// }
	// return resultList;
	// }
	//
	// public static List findCheckouts(ClearCaseInterface ccase, String path)
	// throws ClearCaseException
	// {
	// // Faster to find all checkouts, and filter on path of interest, than it
	// is to find checkouts for subtree.
	// List resultList = new ArrayList();
	//
	// try
	// {
	// File prefixFile = new File(path);
	// String prefix = prefixFile.getCanonicalPath();
	// int slashIdx = prefix.indexOf(File.separator);
	// String prefixNoDrive = prefix.substring(slashIdx);
	// String drive = prefix.substring(0, slashIdx);
	//
	// ClearCaseInterface.Status viewNameStatus = ccase.getViewName(prefix);
	// if (!viewNameStatus.status)
	// throw new Exception(viewNameStatus.message);
	// String viewName = viewNameStatus.message.trim();
	//
	// boolean isSnapShot = ccase.isSnapShot(prefix,
	// ccase.isSymbolicLink(prefix));
	// boolean projectHasViewPath = prefix.indexOf(viewName) != -1;
	//
	// ClearCaseInterface.Status result =
	// ccase.cleartool("lsco -me -cview -short -all " + quote(prefix));
	// if (!result.status)
	// throw new Exception(result.message);
	//
	// StringTokenizer st = new StringTokenizer(result.message, "\r\n");
	// while (st.hasMoreTokens())
	// {
	// String entry = st.nextToken();
	// // If snapshot, or dynamic but path is in clearcase "views" directory,
	// // then just add the filename verbatim, otherwise we need to clean it up
	// by remapping
	// // to the same drive/etc as path passed in.
	// if (isSnapShot || projectHasViewPath)
	// {
	// resultList.add(entry);
	// }
	// else
	// {
	// int idx = entry.indexOf(viewName);
	// String cleanEntry;
	// if (idx == -1)
	// {
	// cleanEntry = entry;
	// }
	// else
	// {
	// idx += viewName.length();
	// cleanEntry = entry.substring(idx);
	// }
	// if (cleanEntry.startsWith(prefixNoDrive))
	// resultList.add(drive + cleanEntry);
	// }
	//
	// }
	//
	// Collections.sort(resultList);
	// }
	// catch (Exception e)
	// {
	// throw new ClearCaseException(
	// "Could not find checkouts for path: " + path + ", reason: " + e);
	// }
	//
	// return resultList;
	// }
	//
	// public static List findLabels(ClearCaseInterface ccase, String vobPath)
	// throws ClearCaseException
	// {
	// List resultList = new ArrayList();
	// ClearCaseInterface.Status result =
	// ccase.cleartool("lstype -s -kind lbtype -invob " + quote(vobPath));
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// StringTokenizer st = new StringTokenizer(result.message, "\r\n");
	// while (st.hasMoreTokens())
	// {
	// resultList.add(st.nextToken());
	// }
	// return resultList;
	// }
	//
	// public static List findVOBs(ClearCaseInterface ccase) throws
	// ClearCaseException
	// {
	// List resultList = new ArrayList();
	// ClearCaseInterface.Status result = ccase.cleartool("lsvob -s");
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// StringTokenizer st = new StringTokenizer(result.message, "\r\n");
	// while (st.hasMoreTokens())
	// {
	// resultList.add(st.nextToken());
	// }
	// return resultList;
	// }
	//
	// public static void createLabel(ClearCaseInterface ccase, String label,
	// String vobName)
	// throws ClearCaseException
	// {
	// ClearCaseInterface.Status result = ccase.cleartool("mklbtype -nc " +
	// quote(label));
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// }
	//
	// public static void applyLabel(ClearCaseInterface ccase, String label,
	// List elements, boolean recurse)
	// throws ClearCaseException
	// {
	// for (Iterator iter = elements.iterator(); iter.hasNext();)
	// {
	// String each = iter.next().toString();
	// // make the label - if it already exists, we'll fail silently
	// ccase.cleartool("mklbtype -nc " + quote(label + "@" + each));
	// ClearCaseInterface.Status result =
	// ccase.cleartool(
	// "mklabel -replace "
	// + (recurse ? "-recurse " : "")
	// + quote(label)
	// + " "
	// + quote(each));
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// }
	// }
	//
	// public static String applyCommand(
	// ClearCaseInterface ccase,
	// String command,
	// List elements,
	// boolean applyIndividually)
	// throws ClearCaseException
	// {
	// StringBuffer resultBuffer = new StringBuffer();
	// StringBuffer cmdBuffer = new StringBuffer();
	// cmdBuffer.append(command);
	//
	// for (Iterator iter = elements.iterator(); iter.hasNext();)
	// {
	// String each = iter.next().toString();
	// if (applyIndividually)
	// {
	// ClearCaseInterface.Status result = ccase.cleartool(command + " " +
	// quote(each));
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// resultBuffer.append(result.message);
	// resultBuffer.append(System.getProperty("line.separator"));
	// }
	// else
	// {
	// cmdBuffer.append(" ");
	// cmdBuffer.append(each);
	// }
	// }
	// if (!applyIndividually)
	// {
	// ClearCaseInterface.Status result = ccase.cleartool(cmdBuffer.toString());
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// resultBuffer.append(result.message);
	// }
	// return resultBuffer.toString();
	// }
	//
	// public static List cleartool(ClearCaseInterface ccase, String command)
	// throws ClearCaseException
	// {
	// List resultList = new ArrayList();
	// ClearCaseInterface.Status result = ccase.cleartool(command);
	// if (!result.status)
	// throw new ClearCaseException(result.message);
	// StringTokenizer st = new StringTokenizer(result.message, "\r\n");
	// while (st.hasMoreTokens())
	// {
	// resultList.add(st.nextToken());
	// }
	// return resultList;
	// }
	//
	// public static void add(ClearCaseInterface ccase, File file, String
	// comment, boolean makeMaster) throws ClearCaseException
	// {
	// // Sanity check - can't add something that already is under VC
	// if (ccase.isElement(file.toString()))
	// {
	// throw new ClearCaseException(
	// "Cannot add an element already under version control: " +
	// file.toString());
	// }
	// // Walk up parent heirarchy, find first ccase
	// // element that is a parent, and walk back down, adding each to ccase
	// File parent = file.getParentFile();
	//
	// if (parent == null)
	// {
	// throw new ClearCaseException("Only files with some parent that is a
	// clearcase element can be added to clearcase.");
	// }
	//
	// // If parent is in clearcase, check it out, otherwise try and add it with
	// // a recursive call
	// if (ccase.isElement(parent.toString()))
	// {
	// if
	// (!ccase.isCheckedOut(parent.toString(),ccase.isSymbolicLink(parent.toString())))
	// {
	// Status result = ccase.checkout(parent.toString(), comment, false, true);
	// if (!result.status)
	// {
	// throw new ClearCaseException(
	// "Could not checkout: " + parent + " due to: " + result.message);
	// }
	// }
	// }
	// else
	// {
	// add(ccase, parent, comment, makeMaster);
	// }
	//
	// if (file.isDirectory())
	// {
	// String path = file.toString();
	// File origfolder = new File(path);
	// File mkelemfolder = new File(path + ".mkelem");
	// origfolder.renameTo(mkelemfolder);
	// Status status = ccase.add(path, comment, true, makeMaster);
	// if (status.status)
	// {
	// File[] members = mkelemfolder.listFiles();
	// for (int i = 0; i < members.length; i++)
	// {
	// File member = members[i];
	// File newMember = new File(origfolder.getPath(), member.getName());
	// member.renameTo(newMember);
	// }
	// mkelemfolder.delete();
	// }
	// else
	// {
	// throw new ClearCaseException("Add failed: " + status.message);
	// }
	// }
	// else
	// {
	// Status status = ccase.add(file.toString(), comment, false, makeMaster);
	// if (!status.status)
	// {
	// throw new ClearCaseException("Add failed: " + status.message);
	// }
	// }
	// }
	//
}