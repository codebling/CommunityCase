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
package org.community.intellij.plugins.communitycase.commands;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer;
import org.community.intellij.plugins.communitycase.Util;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * File utilities
 */
public class FileUtils {
  /**
   * If multiple paths are specified on the command line, this limit is used to split paths into chunks.
   * The limit is less than OS limit to leave space to quoting, spaces, charset conversion, and commands arguments.
   */
  public static final int FILE_PATH_LIMIT = 7600;

  /**
   * The private constructor for static utility class
   */
  private FileUtils() {
    // do nothing
  }

  /**
   * Chunk paths on the command line
   *
   * @param files the paths to chunk
   * @return the a list of list of relative paths
   */
  public static List<List<String>> chunkRelativePaths(List<String> files) {
    ArrayList<List<String>> rc = new ArrayList<List<String>>();
    int start = 0;
    int size = 0;
    int i = 0;
    for (; i < files.size(); i++) {
      String p = files.get(i);
      if (size + p.length() > FILE_PATH_LIMIT) {
        if (start == i) {
          rc.add(files.subList(i, i + 1));
          start = i + 1;
        }
        else {
          rc.add(files.subList(start, i));
          start = i;
        }
        size = 0;
      }
      else {
        size += p.length();
      }
    }
    if (start != files.size()) {
      rc.add(files.subList(start, i));
    }
    return rc;
  }

  /**
   * The chunk paths
   *
   * @param root  the vcs root
   * @param files the file list
   * @return chunked relative paths
   */
  public static List<List<String>> chunkPaths(VirtualFile root, Collection<FilePath> files) {
    return chunkRelativePaths(Util.toRelativePaths(root, files));
  }

  /**
   * The chunk paths
   *
   * @param root  the vcs root
   * @param files the file list
   * @return chunked relative paths
   */
  public static List<List<String>> chunkFiles(VirtualFile root, Collection<VirtualFile> files) {
    return chunkRelativePaths(Util.toRelativeFiles(root, files));
  }

  /**
   * Delete files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to delete
   * @return a result of operation
   * @throws VcsException in case of git problem
   */

  public static void delete(Project project, VirtualFile root, Collection<FilePath> files, String... additionalOptions)
    throws VcsException {
    for (List<String> paths : chunkPaths(root, files)) {
      SimpleHandler handler = new SimpleHandler(project, root, Command.RM);
      handler.addParameters(additionalOptions);
      handler.endOptions();
      handler.addParameters(paths);
      handler.setRemote(true);
      handler.run();
    }
  }

  public static void cherryPick(final Project project, final VirtualFile root, final String hash) throws VcsException {
    SimpleHandler handler = new SimpleHandler(project, root, Command.CHERRY_PICK);
    handler.addParameters("-x", "-n", hash);
    handler.endOptions();
    //handler.addRelativePaths(new FilePathImpl(root));
    handler.setRemote(true);
    handler.run();
  }

  /**
   * Delete files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to delete
   * @return a result of operation
   * @throws VcsException in case of git problem
   */
  public static void deleteFiles(Project project, VirtualFile root, List<VirtualFile> files) throws VcsException {
    for (List<String> paths : chunkFiles(root, files)) {
      SimpleHandler handler = new SimpleHandler(project, root, Command.RM);
      handler.endOptions();
      handler.addParameters(paths);
      handler.setRemote(true);
      handler.run();
    }
  }

  /**
   * Delete files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to delete
   * @return a result of operation
   * @throws VcsException in case of git problem
   */
  public static void deleteFiles(Project project, VirtualFile root, VirtualFile... files) throws VcsException {
    deleteFiles(project, root, Arrays.asList(files));
  }

  /**
   * Add/index files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a result of operation
   * @throws VcsException in case of git problem
   */
  public static void addFiles(Project project, VirtualFile root, Collection<VirtualFile> files) throws VcsException {
    for (List<String> paths : chunkFiles(root, files)) {
      SimpleHandler handler = new SimpleHandler(project, root, Command.ADD);
      handler.endOptions();
      handler.addParameters(paths);
      handler.setRemote(true);
      handler.run();
    }
  }

  /**
   * Add/index files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a result of operation
   * @throws VcsException in case of git problem
   */
  public static void addFiles(Project project, VirtualFile root, VirtualFile... files) throws VcsException {
    addFiles(project, root, Arrays.asList(files));
  }

  /**
   * Add/index files
   *
   * @param project the project
   * @param root    a vcs root
   * @param files   files to add
   * @return a result of operation
   * @throws VcsException in case of git problem
   */
  public static void addPaths(Project project, VirtualFile root, Collection<FilePath> files) throws VcsException {
    for (List<String> paths : chunkPaths(root, files)) {
      SimpleHandler handler = new SimpleHandler(project, root, Command.ADD);
      handler.endOptions();
      handler.addParameters(paths);
      handler.setRemote(true);
      handler.run();
    }
  }

  /**
   * Get file content for the specific revision
   *
   * @param project      the project
   * @param root         the vcs root
   * @param revisionOrBranch     the revision to find path in or branch 
   * @param relativePath
   * @return the content of file if file is found, null if the file is missing in the revision
   * @throws VcsException if there is a problem with running git
   */
  @Nullable
  public static byte[] getFileContent(Project project, VirtualFile root, String revisionOrBranch, String relativePath) throws VcsException {
    BinaryHandler h = new BinaryHandler(project, root, Command.SHOW);
    h.setRemote(true);
    h.setSilent(true);

    File temp = null;
    try {
      temp = FileUtil.createTempFile(root.getNameWithoutExtension() + revisionOrBranch.replaceAll("/","-"), "tmp");
    } catch(IOException e) {
      throw new VcsException(e);
    }
    if(temp.exists()) {
      temp.delete();
    }
    temp.deleteOnExit();

    h.addParameters("-to "+temp.getAbsolutePath());

    h.addParameters(relativePath + "@@" + revisionOrBranch);
    byte[] result;
    try {
      result = h.run();
    }
    catch (VcsException e) {
      String m = e.getMessage().trim();
      if (m.startsWith("fatal: ambiguous argument ") || (m.startsWith("fatal: Path '") && m.contains("' exists on disk, but not in '"))) {
        result = null;
      }
      else {
        throw e;
      }
    }
    byte[] bytes=null;
    FileInputStream savedVersionInputStream=null;
    try {
      savedVersionInputStream=new FileInputStream(temp);

      bytes=new byte[savedVersionInputStream.available()];

      int readCount=0;
      readCount=savedVersionInputStream.read(bytes);
      if(readCount!=bytes.length)
        throw new VcsException("Couldn't read appropriate number of bytes");

      /*
      List<byte[]> allBytes = new ArrayList<byte[]>();
      byte[] someBytes = new byte[4048];
      int readCount = 0;

      while(readCount != -1) { // savedVersionInputStream.available()) {
        readCount = savedVersionInputStream.read(someBytes);
        if(readCount > 0) {
          if(readCount!=4048) {
            allBytes.add(Arrays.copyOf(someBytes,readCount));
          }else{ //don't waste time resizing/copying the array if it has the right amount of data in it.
            allBytes.add(someBytes);
          }
        }
      }

      int totalSize=0;
      for(byte[] b:allBytes) {
        totalSize+=b.length;
      }

      byte[] fixedAllBytes=new byte[totalSize];

      for(byte[] b:allBytes) {
        System.arraycopy();
      }
      */

    } catch(IOException e) {
      throw new VcsException(e);
    } finally {
      try {
        savedVersionInputStream.close();
        temp.delete();
      } catch(Exception e) {} //sorry, best effort.
    }

    return bytes;
  }

  /**
   * Returns the GitFileRevision for given parameters.
   * @param revisionOrBranch full hash of the revision, or branch name, or tag name - any will do.
   * @param loadContent should the content be preloaded in the returned VcsFileRevision.
   * @return VcsFileRevision for the given parameters.
   */
  //@Nullable
  //public static VcsFileRevision getFileRevision(Project project, VirtualFile vcsRoot, String revisionOrBranch, String relativePath, boolean loadContent) {
  //  SimpleHandler h = new SimpleHandler(project, vcsRoot, Command.LSHISTORY);
  //  h.setNoSSH(true);
  //  h.setSilent(true);
  //  h.addParameters(revisionOrBranch + ":" + relativePath);
  //
  //  if (!loadContent) {
  //    h.addParameters("--name-only");
  //  }
  //
  //}
  
}
