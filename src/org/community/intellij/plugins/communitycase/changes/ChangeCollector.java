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
 *
 */
package org.community.intellij.plugins.communitycase.changes;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.ContentRevision;
import org.community.intellij.plugins.communitycase.RevisionNumber;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * A collector for changes in version control. It is introduced because changes are not
 * cannot be got as a sum of stateless operations.
 *
 ===================
 ct lsco -a -me -cvi
 5 seconds bamain from anywhere in tree

 format:
 --12-21T17:09  ascher     checkout version "C:\cc\baplugintest\serverdev\lost+fo
und\wrapper_32.6223c92a584d4266bcc1b081259b9b2c" from \main\0 (reserved)
 ===================
 update -print
 9 minutes bamain
 ===================
 ls -r
 35 minutes for bamain
 ===================
 *
 */
class ChangeCollector {
  private static final String VERSION_KEY = "@@";

  private static final Logger log=Logger.getInstance(ChangeCollector.class.getName());
  private final Project myProject;
  private final ChangeListManager myChangeListManager;
  private final ProgressIndicator myProgressIndicator;
  private final VcsDirtyScope myDirtyScope;

  private final VirtualFile myVcsRoot;
  private final List<VirtualFile> myUnversioned = new ArrayList<VirtualFile>(); // Unversioned files
  private final Set<String> myUnmergedNames = new HashSet<String>(); // Names of unmerged files
  private final List<Change> myChanges = new ArrayList<Change>(); // all changes
  private boolean myIsCollected = false; // indicates that collecting changes has been started
  private boolean myIsFailed = true; // indicates that collecting changes has been failed.

  public ChangeCollector(final Project project,
                         ChangeListManager changeListManager,
                         final ProgressIndicator progressIndicator,
                         VcsDirtyScope dirtyScope,
                         final VirtualFile vcsRoot) {
    myChangeListManager = changeListManager;
    myProgressIndicator = progressIndicator;
    myDirtyScope = dirtyScope;
    myVcsRoot = vcsRoot;
    myProject = project;
  }

  /**
   * Get unversioned files
   */
  public Collection<VirtualFile> unversioned() throws VcsException {
    ensureCollected();
    return myUnversioned;
  }

  /**
   * Get changes
   */
  public Collection<Change> changes() throws VcsException {
    ensureCollected();
    return myChanges;
  }


  /**
   * Ensure that changes has been collected.
   */
  private void ensureCollected() throws VcsException {
    if(!myIsCollected) {
      myIsCollected = true;

      collectVcsChanges();
    }
  }

  /**
   * Collect dirty file paths
   *
   * @param includeChanges if true, previous changes are included in collection
   * @return the set of dirty paths to check, the paths are automatically collapsed if the summary length more than limit
   */
  private Collection<FilePath> dirtyPaths(boolean includeChanges) {
    // TODO collapse paths with common prefix
    ArrayList<FilePath> paths = new ArrayList<FilePath>();
    FilePath rootPath = VcsUtil.getFilePath(myVcsRoot.getPath(), true);
    for (FilePath p : myDirtyScope.getRecursivelyDirtyDirectories()) {
      addToPaths(rootPath, paths, p);
    }
    ArrayList<FilePath> candidatePaths = new ArrayList<FilePath>();
    candidatePaths.addAll(myDirtyScope.getDirtyFilesNoExpand());
    if (includeChanges) {
//      try {
        for (Change c : myChangeListManager.getChangesIn(myVcsRoot)) {
          if (c.getAfterRevision() != null) {
            addToPaths(rootPath, paths, c.getAfterRevision().getFile());
          }
          if (c.getBeforeRevision() != null) {
            addToPaths(rootPath, paths, c.getBeforeRevision().getFile());
          }
        }
/*      }
      catch (Exception t) {
        // ignore exceptions
      }
*/
    }
    for (FilePath p : candidatePaths) {
      addToPaths(rootPath, paths, p);
    }
    return paths;
  }

  /**
   * Add path to the collection of the paths to check for this vcs root
   *
   * @param root  the root path
   * @param paths the existing paths
   * @param toAdd the path to add
   */
  void addToPaths(FilePath root, Collection<FilePath> paths, FilePath toAdd) {
    if (Util.getRootOrNull(toAdd) != myVcsRoot) {
      return;
    }
    if (root.isUnder(toAdd, true)) {
      toAdd = root;
    }
    for (Iterator<FilePath> i = paths.iterator(); i.hasNext();) {
      FilePath p = i.next();
      if (isAncestor(toAdd, p, true)) { // toAdd is an ancestor of p => adding toAdd instead of p.
        i.remove();
      }
      if (isAncestor(p, toAdd, false)) { // p is an ancestor of toAdd => no need to add toAdd.
        return;
      }
    }
    paths.add(toAdd);
  }

  /**
   * Returns true if childCandidate file is located under parentCandidate.
   * This is an alternative to {@link com.intellij.openapi.vcs.FilePathImpl#isUnder(com.intellij.openapi.vcs.FilePath, boolean)}:
   * it doesn't check VirtualFile associated with this FilePath.
   * When we move a file we get a VcsDirtyScope with old and new FilePaths, but unfortunately the virtual file in the FilePath is
   * refreshed ({@link com.intellij.openapi.vcs.changes.VirtualFileHolder#cleanAndAdjustScope(com.intellij.openapi.vcs.changes.VcsModifiableDirtyScope)}
   * and thus points to the new position which makes FilePathImpl#isUnder useless.
   *
   * @param parentCandidate FilePath which we check to be the parent of childCandidate.
   * @param childCandidate  FilePath which we check to be a child of parentCandidate.
   * @param strict          if false, the method also returns true if files are equal
   * @return true if childCandidate is a child of parentCandidate.
   */
  private static boolean isAncestor(FilePath parentCandidate, FilePath childCandidate, boolean strict) {
    try {
      return FileUtil.isAncestor(parentCandidate.getIOFile(), childCandidate.getIOFile(), strict);
    }
    catch (IOException e) {
      return false;
    }
  }

  /**
   * Collect all changes
   *
   * @throws VcsException if there is a problem with running
   */
  private void collectVcsChanges() throws VcsException {
    Collection<FilePath> dirtyPaths = dirtyPaths(true);
    if (dirtyPaths.isEmpty()) {
      return;
    }
    // prepare handler
//    LineHandler ls = new LineHandler(myProject, myVcsRoot, Command.LS);
    SimpleHandler ls= new SimpleHandler(myProject, myVcsRoot, Command.LS);
    ls.setRemote(true);
    //ls.setSilent(true);
    //ls.setStdoutSuppressed(true);
    ls.addParameters("-r -vis"); //-vis will not list deleted files
    //ls.addParameters("-r");
    ls.endOptions();
    if(!ls.isAddedPathSizeTooGreat(dirtyPaths))
      ls.addRelativePaths(dirtyPaths);

    // run handler and collect changes

//    HandlerUtil.runInCurrentThread(ls, myProgressIndicator, false, "VCS refresh");
//    parseLsOutput(ls.getStdout());

//    HandlerUtil.doSynchronously(ls, "VCS refresh", "VCS refresh");
//    parseLsOutput(ls.getStdout());

    //parseLsOutput(ls.run());

    SimpleHandler lsco= new SimpleHandler(myProject, myVcsRoot, Command.LS_CHECKOUTS);
    lsco.setRemote(true);
    //lsco.setSilent(true);
    //lsco.setStdoutSuppressed(true);
    lsco.addParameters("-a -me -cvi");
    lsco.endOptions();

    parseLsCheckoutsOutput(lsco.run());
  }

  private VirtualFile createFileIfInRoot(String filename) throws VcsException {
    VirtualFile file=myVcsRoot.findFileByRelativePath(Util.unescapePath(filename));
    if(file==null)
      file=myVcsRoot.findFileByRelativePath(Util.unescapePath(Util.relativePath(myVcsRoot,
                                                                                new FilePathImpl(new File(filename),
                                                                                                 false))));
    if(VcsUtil.getVcsRootFor(myProject,file) == myVcsRoot)
      return file;
    else
      return null;
  }
  private boolean addFileToListIfExistsAndInRoot(List<VirtualFile> list, String filename) {
    boolean failed=false;
    VirtualFile file=null;
    try {
      file=createFileIfInRoot(filename);
    } catch(VcsException e) {
      failed=true;
    }
    if(file!=null && !failed) {
      list.add(file);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Parse Command.LS_CHECKOUTS output.
   * @param list
   * @throws VcsException
   */
  private void parseLsCheckoutsOutput(String list) throws VcsException {
    //Line format:
//--12-21T17:09  ascher     checkout version "C:\cc\baplugintest\serverdev\lost+found\wrapper_32.6223c92a584d4266bcc1b081259b9b2c" from \main\0 (reserved)
    BufferedReader reader=new BufferedReader(new StringReader(list));

    final String filenameStartToken="checkout version \"";
    final String filenameEndToken="\" from ";

    while(true) {
      String line;
      try {
        line=reader.readLine();
      } catch(IOException e) {
        log.error(e);
        break;
      }
      if(line==null) {
        break;
      } else {
        int filenameStart=line.indexOf(filenameStartToken);
        int filenameEnd=line.lastIndexOf(filenameEndToken);
        if(filenameStart>0 && filenameEnd>0 && filenameEnd-filenameStart>0) {
          String filename=line.substring(filenameStart+filenameStartToken.length(), filenameEnd);
          VirtualFile file=createFileIfInRoot(filename);
          if(file != null) {
            //this is a checked out file, which we'll automatically consider to be "modified"
            //in this case, the next string after "from" should be the version number that the checkout came from

            String[] parts=line.substring(filenameEnd+filenameEndToken.length(), line.length()).split("\\s+",0);
            String relativeFilename=Util.relativePath(myVcsRoot,file);

            com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myVcsRoot,
                                                                                                   relativeFilename,
                                                                                                   new RevisionNumber(parts[0]),
                                                                                                   myProject,
                                                                                                   false,
                                                                                                   true);
            com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myVcsRoot,
                                                                                                  relativeFilename,
                                                                                                  null,
                                                                                                  myProject,
                                                                                                  false,
                                                                                                  true);
            myChanges.add(new Change(before, after, FileStatus.MODIFIED));
          }
        }
      }
    }
  }

  /**
   * Parse Command.LS output.
   * @param list
   * @throws VcsException
   */
  private void parseLsOutput(String list) throws VcsException {
    //Line format:
//    wrapper_32.6223c92a584d4266bcc1b081259b9b2c@@\main\CHECKEDOUT from \main\0               Rule: CHECKEDOUT                   //modified
//    wrapper_diameter_loopback.conf.375035980431452ba39e23f44acd7567@@\main\0 [hijacked]      Rule: \main\LATEST                 //modified
//    lost+found.iml                                                                                                              //unversioned (added)
//    wrapper_yahooservices_out.conf.72bc32ce8b4a417b9961dda95d7799bf@@\main\0 [not loaded]    Rule: \main\LATEST                 //(ignored)
//    rapper_yahooservices_out.conf.72bc32ce8b4a417b9961dda95d7799bf@@\main\0 [loaded but missing]            Rule: \main\LATEST  //deleted
    BufferedReader reader=new BufferedReader(new StringReader(list));

    String line;
    int versionStartIndex; //the position of the start of the version number in the pname (pname is file+version)
    String filename;

    while(true) {
      line=null;
      versionStartIndex=-2; //bogus value which will be overwritten
      filename=null;

      try {
        line=reader.readLine();
      } catch(IOException e) {
        log.error(e);
        break;
      }

      //a lot of ifs and if-else but I was hoping the flow would be easier to follow than having 'continue' statements everywhere (it may not be)
      if(line==null) {
        break;
      } else {
        //look for the symbols that denote the start of the version number
        versionStartIndex=line.lastIndexOf(VERSION_KEY); //search for ver # from the back in case filename contains @@

        if(versionStartIndex==-1) {
          //the version was not found, so the line looks like this:
          //lost+found.iml
          //this is what unversioned files look like. Add it to the unversioned list...
          filename=line;
          addFileToListIfExistsAndInRoot(myUnversioned,filename);
        } else {
          if(versionStartIndex > 0) {//basic sanity
            //we did find the version tag. Our line looks like one of these:
            //file1.ext@@\main\CHECKEDOUT from \main\0               Rule: CHECKEDOUT     //modified
            //file2.ext@@\main\0 [hijacked]      Rule: \main\LATEST                       //modified
            //file3.ext@@\main\0 [loaded but missing]            Rule: \main\LATEST       //deleted
            //file4.ext@@\main\0 [not loaded]    Rule: \main\LATEST                       //(ignored)
            //file5.ext                                                                   //unversioned (added)

            filename=line.substring(0,versionStartIndex); //copy the file name
            //now split everything beyond the version marking at each whitespace
            String[] parts=line.substring(versionStartIndex+VERSION_KEY.length(), line.length()).split("\\s+",0);
            if(parts.length >= 3) { //the version, the status/checkout version and the Rule at minimum, longer in some cases
              String version=parts[0]; //copy the version
              VirtualFile file=createFileIfInRoot(filename);
              if(file != null) {
                String relativeFilename=Util.relativePath(myVcsRoot,file);
                if(parts[1].equals("from")) {
                  //this is a checked out file, which we'll automatically consider to be "modified"
                  //in this case, the next string after "from" should be the version number that the checkout came from
                  com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myVcsRoot,relativeFilename,new RevisionNumber(parts[2]),myProject,false,true);
                  //com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myVcsRoot, relativeFilename, new RevisionNumber(version), myProject, false, true);
                  com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myVcsRoot,relativeFilename,null,myProject,false,true);
                  myChanges.add(new Change(before, after, FileStatus.MODIFIED));
                } else {
                  if(parts[1].equals("[hijacked]")) {
                    //wrapper_diameter_loopback.conf.375035980431452ba39e23f44acd7567@@\main\0 [hijacked]      Rule: \main\LATEST
                    com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myVcsRoot, relativeFilename, new RevisionNumber(version), myProject, false, true);
                    com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myVcsRoot, relativeFilename, null, myProject, false, true);
                    myChanges.add(new Change(before, after, FileStatus.HIJACKED));
                  }
                }
              } else {
                if(parts.length >= 4 && parts[1].equals("[loaded") && parts[2].equals("but") && parts[3].equals("missing]")) {
                  //wrapper_yahooservices_out.conf.72bc32ce8b4a417b9961dda95d7799bf@@\main\0 [not loaded]    Rule: \main\LATEST
                  com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myVcsRoot, filename, new RevisionNumber(version), myProject, false, true);
                  com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myVcsRoot, filename, null, myProject, true, true);
                  myChanges.add(new Change(before, after, FileStatus.DELETED_FROM_FS));
                } else {
                  //default/ignored.
                  //if(parts[1].equals("[not") && parts[2].equals("loaded]")) {} //ignored
                }
              }
            }
          }
        }
      }
    }
  }

}
