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
package org.community.intellij.plugins.communitycase;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vcs.vfs.AbstractVcsVirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.changes.ChangeUtils;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.commands.StringScanner;
import org.community.intellij.plugins.communitycase.config.ConfigUtil;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *  utility/helper methods
 */
public class Util {
  /**
   * The logger instance
   */
  private final static Logger LOG = Logger.getInstance("#org.community.intellij.plugins.communitycase.Util");
  /**
   * Comparator for virtual files by name
   */
  public static final Comparator<VirtualFile> VIRTUAL_FILE_COMPARATOR = new Comparator<VirtualFile>() {
    public int compare(final VirtualFile o1, final VirtualFile o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      return o1.getPresentableUrl().compareTo(o2.getPresentableUrl());
    }
  };
  /**
   * The UTF-8 encoding name
   */
  public static final String UTF8_ENCODING = "UTF-8";
  /**
   * The UTF8 charset
   */
  public static final Charset UTF8_CHARSET = Charset.forName(UTF8_ENCODING);

  /**
   * A private constructor to suppress instance creation
   */
  private Util() {
    // do nothing
  }

  /**
   * Sort files by  root
   *
   * @param virtualFiles files to sort
   * @return sorted files
   * @throws com.intellij.openapi.vcs.VcsException if non  files are passed
   */
  @NotNull
  public static Map<VirtualFile, List<VirtualFile>> sortFilesByRoot(@NotNull Collection<VirtualFile> virtualFiles) throws VcsException {
    return sortFilesByRoot(virtualFiles, false);
  }

  /**
   * Sort files by  root
   *
   * @param virtualFiles files to sort
   * @param ignoreNon if true, non- files are ignored
   * @return sorted files
   * @throws com.intellij.openapi.vcs.VcsException if non  files are passed when {@code ignoreNon} is false
   */
  public static Map<VirtualFile, List<VirtualFile>> sortFilesByRoot(Collection<VirtualFile> virtualFiles, boolean ignoreNon)
    throws VcsException {
    Map<VirtualFile, List<VirtualFile>> result = new HashMap<VirtualFile, List<VirtualFile>>();
    for (VirtualFile file : virtualFiles) {
      final VirtualFile vcsRoot = rootOrNull(file);
      if (vcsRoot == null) {
        if (ignoreNon) {
          continue;
        }
        else {
          throw new VcsException("The file " + file.getPath() + " is not under ");
        }
      }
      List<VirtualFile> files = result.get(vcsRoot);
      if (files == null) {
        files = new ArrayList<VirtualFile>();
        result.put(vcsRoot, files);
      }
      files.add(file);
    }
    return result;
  }

  public static String getRelativeFilePath(VirtualFile file, @NotNull final VirtualFile baseDir) {
    return getRelativeFilePath(file.getPath(), baseDir);
  }

  public static String getRelativeFilePath(FilePath file, @NotNull final VirtualFile baseDir) {
    return getRelativeFilePath(file.getPath(), baseDir);
  }

  public static String getRelativeFilePath(String file, @NotNull final VirtualFile baseDir) {
    if (SystemInfo.isWindows) {
      file = file.replace('\\', '/');
    }
    final String basePath = baseDir.getPath();
    if (!file.startsWith(basePath)) {
      return file;
    }
    else if (file.equals(basePath)) return ".";
    return file.substring(baseDir.getPath().length() + 1);
  }

  /**
   * Sort files by vcs root
   *
   * @param files files to sort.
   * @return the map from root to the files under the root
   * @throws com.intellij.openapi.vcs.VcsException if non  files are passed
   */
/*  public static Map<VirtualFile, List<FilePath>> sortFilePathsByRoot(final Collection<FilePath> files) throws VcsException {
    return sortFilePathsByRoot(files, false);
  }
*/
  /**
   * Sort files by vcs root
   *
   * @param files files to sort.
   * @return the map from root to the files under the root
   */
  public static Map<VirtualFile, List<FilePath>> sortFilePathsByRoot(Collection<FilePath> files) throws VcsException {
    return sortFilePathsByRoot(files, true);
  }


  /**
   * Sort files by vcs root
   *
   * @param files        files to sort.
   * @param ignoreNon if true, non- files are ignored
   * @return the map from root to the files under the root
   * @throws com.intellij.openapi.vcs.VcsException if non  files are passed when {@code ignoreNon} is false
   */
  public static Map<VirtualFile, List<FilePath>> sortFilePathsByRoot(Collection<FilePath> files, boolean ignoreNon)
    throws VcsException {
    Map<VirtualFile, List<FilePath>> rc = new HashMap<VirtualFile, List<FilePath>>();
    for (FilePath p : files) {
      VirtualFile root = getRootOrNull(p);
      if (root == null) {
        if (ignoreNon) {
          continue;
        }
        else {
          throw new VcsException("The file " + p.getPath() + " is not under ");
        }
      }
      List<FilePath> l = rc.get(root);
      if (l == null) {
        l = new ArrayList<FilePath>();
        rc.put(root, l);
      }
      l.add(p);
    }
    return rc;
  }

  /**
   * Unescape path returned by the
   *
   * @param path a path to unescape
   * @return unescaped path
   * @throws com.intellij.openapi.vcs.VcsException if the path in invalid
   */
  public static String unescapePath(String path) throws VcsException {
    if (File.separatorChar != '/')
      return path.replace(File.separatorChar, '/');
    else
      return path;

    //shouldn't be needed as this was only to unescape paths whose names were escaped by Git. ClearCase doesn't escape paths.
    /*
    final int l = path.length();
    StringBuilder rc = new StringBuilder(l);
    for (int i = 0; i < path.length(); i++) {
      char c = path.charAt(i);
      if (c == '\\') {
        //noinspection AssignmentToForLoopParameter
        i++;
        if (i >= l) {
          throw new VcsException("Unterminated escape sequence in the path: " + path);
        }
        final char e = path.charAt(i);
        switch (e) {
          case '\\':
            rc.append('\\');
            break;
          case 't':
            rc.append('\t');
            break;
          case 'n':
            rc.append('\n');
            break;
          default:
            if (isOctal(e)) {
              // collect sequence of characters as a byte array.
              // count bytes first
              int n = 0;
              for (int j = i; j < l;) {
                if (isOctal(path.charAt(j))) {
                  n++;
                  for (int k = 0; k < 3 && j < l && isOctal(path.charAt(j)); k++) {
                    //noinspection AssignmentToForLoopParameter
                    j++;
                  }
                }
                if (j + 1 >= l || path.charAt(j) != '\\' || !isOctal(path.charAt(j + 1))) {
                  break;
                }
                //noinspection AssignmentToForLoopParameter
                j++;
              }
              // convert to byte array
              byte[] b = new byte[n];
              n = 0;
              while (i < l) {
                if (isOctal(path.charAt(i))) {
                  int code = 0;
                  for (int k = 0; k < 3 && i < l && isOctal(path.charAt(i)); k++) {
                    code = code * 8 + (path.charAt(i) - '0');
                    //noinspection AssignmentToForLoopParameter
                    i++;
                  }
                  b[n++] = (byte)code;
                }
                if (i + 1 >= l || path.charAt(i) != '\\' || !isOctal(path.charAt(i + 1))) {
                  break;
                }
                //noinspection AssignmentToForLoopParameter
                i++;
              }
              assert n == b.length;
              // add them to string
              final String encoding = ConfigUtil.getFileNameEncoding();
              try {
                rc.append(new String(b, encoding));
              }
              catch (UnsupportedEncodingException e1) {
                throw new IllegalStateException("The file name encoding is unsuported: " + encoding);
              }
            }
            else {
              throw new VcsException("Unknown escape sequence '\\" + path.charAt(i) + "' in the path: " + path);
            }
        }
      }
      else {
        rc.append(c);
      }
    }
    return rc.toString();
    */
  }

  /**
   * Check if character is octal di
   *
   * @param ch a character to test
   * @return true if the octal di, false otherwise
   */
  private static boolean isOctal(char ch) {
    return '0' <= ch && ch <= '7';
  }

  /**
   * Parse UNIX timestamp as it is returned by the
   *
   * @param value a value to parse
   * @return timestamp as {@link java.util.Date} object
   */
  public static Date parseTimestamp(String value) {
    //20101116.152312
    try {
      return new SimpleDateFormat("yyyyMMdd.HHmmss").parse(value);
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get  roots from content roots
   *
   * @param roots  content roots
   * @return a content root
   */
  public static Set<VirtualFile> rootsForPaths(final Collection<VirtualFile> roots) {
    HashSet<VirtualFile> rc = new HashSet<VirtualFile>();
    for (VirtualFile root : roots) {
      VirtualFile f = root;
      do {
        if (f.findFileByRelativePath(".") != null) {
          rc.add(f);
          break;
        }
        f = f.getParent();
      }
      while (f != null);
    }
    return rc;
  }

  /**
   * Return a  root for the file path (the parent directory with "." subdirectory)
   *
   * @param filePath a file path
   * @return  root for the file
   * @throws IllegalArgumentException if the file is not under
   * @throws com.intellij.openapi.vcs.VcsException             if the file is not under
   */
  public static VirtualFile getRoot(final FilePath filePath) throws VcsException {
    VirtualFile root = getRootOrNull(filePath);
    if (root != null) {
      return root;
    }
    throw new VcsException("The file " + filePath + " is not under .");
  }

  /**
   * Return a  root for the file path (the parent directory with "." subdirectory)
   *
   * @param filePath a file path
   * @return  root for the file or null if the file is not under
   */
  @Nullable
  public static VirtualFile getRootOrNull(final FilePath filePath) {
    File file = filePath.getIOFile();
    while (file != null && (!file.exists() || !file.isDirectory() || !new File(file, ".").exists())) {
      file = file.getParentFile();
    }
    if (file == null) {
      return null;
    }
    return LocalFileSystem.getInstance().findFileByIoFile(file);
  }

  /**
   * Return a root for the file (the parent directory with "." subdirectory)
   *
   * @param file the file to check
   * @return  root for the file
   * @throws com.intellij.openapi.vcs.VcsException if the file is not under
   */
  public static VirtualFile getRoot(@NotNull final VirtualFile file) throws VcsException {
    final VirtualFile root = rootOrNull(file);
    if (root != null) {
      return root;
    }
    else {
      throw new VcsException("The file " + file.getPath() + " is not under .");
    }
  }

  /**
   * Return a  root for the file (the parent directory with "." subdirectory)
   *
   * @param file the file to check
   * @return  root for the file or null if the file is not not under
   */
  @Nullable
  public static VirtualFile rootOrNull(final VirtualFile file) {
    if (file instanceof AbstractVcsVirtualFile) {
      return getRootOrNull(VcsUtil.getFilePath(file.getPath()));
    }
    VirtualFile root = file;
    while (root != null) {
      if (root.findFileByRelativePath(".") != null) {
        return root;
      }
      root = root.getParent();
    }
    return root;
  }

  /**
   * Get  roots for the project. The method shows dialogs in the case when roots cannot be retrieved, so it should be called
   * from the event dispatch thread.
   *
   * @param project the project
   * @param vcs     the  Vcs
   * @return the list of the roots
   */
  @NotNull
  public static List<VirtualFile> getRoots(Project project, Vcs vcs) throws VcsException {
    final VirtualFile[] contentRoots = ProjectLevelVcsManager.getInstance(project).getRootsUnderVcs(vcs);
    if (contentRoots == null || contentRoots.length == 0) {
      throw new VcsException(Bundle.getString("repository.action.missing.roots.unconfigured.message"));
    }
    final List<VirtualFile> roots = new ArrayList<VirtualFile>(rootsForPaths(Arrays.asList(contentRoots)));
    if (roots.size() == 0) {
      throw new VcsException(Bundle.getString("repository.action.missing.roots.misconfigured"));
    }
    Collections.sort(roots, VIRTUAL_FILE_COMPARATOR);
    return roots;
  }


  /**
   * Check if the virtual file under
   *
   * @param vFile a virtual file
   * @return true if the file is under
   */
  public static boolean isUnder(final VirtualFile vFile) {
    return rootOrNull(vFile) != null;
  }

  /**
   * Get relative path
   *
   * @param root a root path
   * @param path a path to file (possibly deleted file)
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativePath(final VirtualFile root, FilePath path) {
    return relativePath(VfsUtil.virtualToIoFile(root), path.getIOFile());
  }


  /**
   * Get relative path
   *
   * @param root a root path
   * @param path a path to file (possibly deleted file)
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativePath(final File root, FilePath path) {
    return relativePath(root, path.getIOFile());
  }

  /**
   * Get relative path
   *
   * @param root a root path
   * @param file a virtual file
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativePath(final File root, VirtualFile file) {
    return relativePath(root, VfsUtil.virtualToIoFile(file));
  }

  /**
   * Get relative path
   *
   * @param root a root file
   * @param file a virtual file
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativePath(final VirtualFile root, VirtualFile file) {
    return relativePath(VfsUtil.virtualToIoFile(root), VfsUtil.virtualToIoFile(file));
  }

  /**
   * Get relative path
   *
   * @param root a root file
   * @param file a virtual file
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativeOrFullPath(final VirtualFile root, VirtualFile file) {
    if (root == null) {
      file.getPath();
    }
    return relativePath(VfsUtil.virtualToIoFile(root), VfsUtil.virtualToIoFile(file));
  }

  /**
   * Get relative path
   *
   * @param root a root path
   * @param path a path to file (possibly deleted file)
   * @return a relative path
   * @throws IllegalArgumentException if path is not under root.
   */
  public static String relativePath(final File root, File path) {
    String rc = FileUtil.getRelativePath(root, path);
    if (rc == null) {
      throw new IllegalArgumentException("The file " + path + " cannot be made relative to " + root);
    }
    return rc.replace(File.separatorChar, '/');
  }

  /**
   * Covert list of files to relative paths
   *
   * @param root      a vcs root
   * @param filePaths a parameters to convert
   * @return a list of relative paths
   * @throws IllegalArgumentException if some path is not under root.
   */
  public static List<String> toRelativePaths(@NotNull VirtualFile root, @NotNull final Collection<FilePath> filePaths) {
    ArrayList<String> rc = new ArrayList<String>(filePaths.size());
    for (FilePath path : filePaths) {
      rc.add(relativePath(root, path));
    }
    return rc;
  }

  /**
   * Covert list of files to relative paths
   *
   * @param root  a vcs root
   * @param files a parameters to convert
   * @return a list of relative paths
   * @throws IllegalArgumentException if some path is not under root.
   */
  public static List<String> toRelativeFiles(@NotNull VirtualFile root, @NotNull final Collection<VirtualFile> files) {
    ArrayList<String> rc = new ArrayList<String>(files.size());
    for (VirtualFile file : files) {
      rc.add(relativePath(root, file));
    }
    return rc;
  }

  /**
   * Refresh files
   *
   * @param project       a project
   * @param affectedFiles affected files and directories
   */
  public static void refreshFiles(@NotNull final Project project, @NotNull final Collection<VirtualFile> affectedFiles) {
    final VcsDirtyScopeManager dirty = VcsDirtyScopeManager.getInstance(project);
    for (VirtualFile file : affectedFiles) {
      if (!file.isValid()) {
        continue;
      }
      file.refresh(false, true);
      if (file.isDirectory()) {
        dirty.dirDirtyRecursively(file);
      }
      else {
        dirty.fileDirty(file);
      }
    }
  }

  /**
   * Refresh files
   *
   * @param project       a project
   * @param affectedFiles affected files and directories
   */
  public static void markFilesDirty(@NotNull final Project project, @NotNull final Collection<VirtualFile> affectedFiles) {
    final VcsDirtyScopeManager dirty = VcsDirtyScopeManager.getInstance(project);
    for (VirtualFile file : affectedFiles) {
      if (!file.isValid()) {
        continue;
      }
      if (file.isDirectory()) {
        dirty.dirDirtyRecursively(file);
      }
      else {
        dirty.fileDirty(file);
      }
    }
  }


  /**
   * Mark files dirty
   *
   * @param project       a project
   * @param affectedFiles affected files and directories
   */
  public static void markFilesDirty(Project project, List<FilePath> affectedFiles) {
    final VcsDirtyScopeManager dirty = VcsDirtyScopeManager.getInstance(project);
    for (FilePath file : affectedFiles) {
      if (file.isDirectory()) {
        dirty.dirDirtyRecursively(file);
      }
      else {
        dirty.fileDirty(file);
      }
    }
  }

  /**
   * Refresh files
   *
   * @param project       a project
   * @param affectedFiles affected files and directories
   */
  public static void refreshFiles(Project project, List<FilePath> affectedFiles) {
    final VcsDirtyScopeManager dirty = VcsDirtyScopeManager.getInstance(project);
    for (FilePath file : affectedFiles) {
      VirtualFile vFile = VcsUtil.getVirtualFile(file.getIOFile());
      if (vFile != null) {
        vFile.refresh(false, true);
      }
      if (file.isDirectory()) {
        dirty.dirDirtyRecursively(file);
      }
      else {
        dirty.fileDirty(file);
      }
    }
  }

  /**
   * Return committer name based on author name and committer name
   *
   * @param authorName    the name of author
   * @param committerName the name of committer
   * @return just a name if they are equal, or name that includes both author and committer
   */
  public static String adjustAuthorName(final String authorName, String committerName) {
    if (!authorName.equals(committerName)) {
      //noinspection HardCodedStringLiteral
      committerName = authorName + ", via " + committerName;
    }
    return committerName;
  }

  /**
   * Check if the file path is under
   *
   * @param path the path
   * @return true if the file path is under
   */
  public static boolean isUnder(final FilePath path) {
    return getRootOrNull(path) != null;
  }

  /**
   * Get  roots for the selected paths
   *
   * @param filePaths the context paths
   * @return a set of  roots
   */
  public static Set<VirtualFile> roots(final Collection<FilePath> filePaths) {
    HashSet<VirtualFile> rc = new HashSet<VirtualFile>();
    for (FilePath path : filePaths) {
      final VirtualFile root = getRootOrNull(path);
      if (root != null) {
        rc.add(root);
      }
    }
    return rc;
  }

  /**
   * Get  time (UNIX time) basing on the date object
   *
   * @param time the time to convert
   * @return the time in  format
   */
  public static String vcsTime(Date time) {
    long t = time.getTime() / 1000;
    return Long.toString(t);
  }

  /**
   * Format revision number from long to 16-di abbreviated revision
   *
   * @param rev the abbreviated revision number as long
   * @return the revision string
   */
  public static String formatLongRev(long rev) {
    return String.format("%015x%x", (rev >>> 4), rev & 0xF);
  }

  /**
   * The get the possible base for the path. It tries to find the parent for the provided path.
   *
   * @param file the file to get base for
   * @param path the path to to check
   * @return the file base
   */
  @Nullable
  public static VirtualFile getPossibleBase(final VirtualFile file, final String... path) {
    if (file == null || path.length == 0) return null;

    VirtualFile current = file;
    final List<VirtualFile> backTrace = new ArrayList<VirtualFile>();
    int idx = path.length - 1;
    while (current != null) {
      if (SystemInfo.isFileSystemCaseSensitive ? current.getName().equals(path[idx]) : current.getName().equalsIgnoreCase(path[idx])) {
        if (idx == 0) {
          return current;
        }
        -- idx;
      } else if (idx != path.length - 1) {
        int diff = path.length - 1 - idx - 1;
        for (int i = 0; i < diff; i++) {
          current = backTrace.remove(backTrace.size() - 1);
        }
        idx = path.length - 1;
        continue;
      }
      backTrace.add(current);
      current = current.getParent();
    }

    return null;
  }

  public static void getLocalCommittedChanges(final Project project,
                                              final VirtualFile root,
                                              final Consumer<SimpleHandler> parametersSpecifier,
                                              final Consumer<CommittedChangeList> consumer, boolean skipDiffsForMerge) throws VcsException {
    SimpleHandler h = new SimpleHandler(project, root, Command.LOG);
    h.setSilent(true);
    h.setRemote(true);
    h.addParameters("--pretty=format:%x00%x01" + ChangeUtils.COMMITTED_CHANGELIST_FORMAT, "--name-status");
    parametersSpecifier.consume(h);

    String output = h.run();
    LOG.debug("getLocalCommittedChanges output: '" + output + "'");
    StringScanner s = new StringScanner(output);
    final StringBuilder sb = new StringBuilder();
    boolean firstStep = true;
    while (s.hasMoreData()) {
      final String line = s.line();
      final boolean lineIsAStart = line.startsWith("\u0000\u0001");
      if ((!firstStep) && lineIsAStart) {
        final StringScanner innerScanner = new StringScanner(sb.toString());
        sb.setLength(0);
        consumer.consume(ChangeUtils.parseChangeList(project, root, innerScanner, skipDiffsForMerge));
      }
      sb.append(lineIsAStart ? line.substring(2) : line).append('\n');
      firstStep = false;
    }
    if (sb.length() > 0) {
      final StringScanner innerScanner = new StringScanner(sb.toString());
      sb.setLength(0);
      consumer.consume(ChangeUtils.parseChangeList(project, root, innerScanner, skipDiffsForMerge));
    }
    if (s.hasMoreData()) {
      throw new IllegalStateException("More input is avaialble: " + s.line());
    }
  }

  public static List<CommittedChangeList> getLocalCommittedChanges(final Project project,
                                                                   final VirtualFile root,
                                                                   final Consumer<SimpleHandler> parametersSpecifier)
    throws VcsException {
    final List<CommittedChangeList> rc = new ArrayList<CommittedChangeList>();

    getLocalCommittedChanges(project, root, parametersSpecifier, new Consumer<CommittedChangeList>() {
      public void consume(CommittedChangeList committedChangeList) {
        rc.add(committedChangeList);
      }
    }, false);

    return rc;
  }

  /**
   * Cast or wrap exception into a vcs exception, errors and runtime exceptions are just thrown throw.
   *
   * @param t an exception to throw
   * @return a wrapped exception
   */
  public static VcsException rethrowVcsException(Throwable t) {
    if (t instanceof Error) {
      throw (Error)t;
    }
    if (t instanceof RuntimeException) {
      throw (RuntimeException)t;
    }
    if (t instanceof VcsException) {
      return (VcsException)t;
    }
    return new VcsException(t.getMessage(), t);
  }
}
