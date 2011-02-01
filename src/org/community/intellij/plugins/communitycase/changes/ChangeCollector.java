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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.popup.BalloonHandler;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.enumeration.ArrayListEnumeration;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.ContentRevision;
import org.community.intellij.plugins.communitycase.RevisionNumber;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.Handler;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

//todo wc clean up this shitty shitty code
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
 *
 * dir not checked out, file checked out,     file deleted      -> deleted (CC won't restore the file on an update)
 * dir not checked out, file not checked out, file deleted      -> missing (CC will restore the file on an update) (deleted from fs status)
 */
class ChangeCollector {
  private static final String VERSION_KEY = "@@";

  private static final Logger log=Logger.getInstance(ChangeCollector.class.getName());
  private final Project myProject;
  private final ChangeListManager myChangeListManager;
  private final ProgressIndicator myProgressIndicator;
  private final VcsDirtyScope myDirtyScope;

  private final ProjectFileIndex myFileIndex;

  private final VirtualFile myVcsRoot;
  private final List<VirtualFile> myUnversioned = new ArrayList<VirtualFile>(); // Unversioned files
  private final Set<String> myUnmergedNames = new HashSet<String>(); // Names of unmerged files
  private final List<Change> myChanges = new ArrayList<Change>(); // all changes
  private boolean myIsCollected = false; // indicates that collecting changes has been started
  private boolean myIsFailed = true; // indicates that collecting changes has been failed.

  private static final int MAX_THREADS=15;
  private final List<RecurseRunnable> myThreads=new ArrayList<RecurseRunnable>();
  private final Vector<VcsException> myExceptions=new Vector<VcsException>();

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
    myFileIndex=ProjectRootManager.getInstance(myProject).getFileIndex();
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

      collectVcsModifiedList();

      if(!DumbService.getInstance(myProject).isDumb()) {  //don't go to town on the HD if we're indexing, causes excessive thrashing
        Collection<VirtualFile> addedOrHijackedOrCheckedOutFiles=Util.stringToVirtualFile(myVcsRoot,
                                                                                          getFsWritableFiles(),
                                                                                          true);
        //we already have all the info we need about checked out files, so remove those from the list
        for(Change change:myChanges)
          addedOrHijackedOrCheckedOutFiles.remove(change.getVirtualFile()); //VirtualFile has no equals method, but since it is unique for this
                                                                            //IntelliJ aka VM instance, we're ok to use Object's default one with the Set (Sets compare elements for equality)
        checkStatusAndAddToChangeList(addedOrHijackedOrCheckedOutFiles);
      } else {  //if we're in dumb mode, trigger a refresh after all files are indexed
        //todo wc don't register this listener several times! Put a variable in ChangeProvider that can remember if there's one installed.
        myProject.getMessageBus().connect().subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
          public void enteredDumbMode() {}
          public void exitDumbMode() {
            //VcsUtil.runVcsProcessWithProgress()
            HashSet<FilePath> paths=new HashSet<FilePath>();
            paths.add(Util.virtualFileToFilePath(myVcsRoot));
            VcsUtil.refreshFiles(myProject, paths);
          }
        });
      }
    }
  }

  private void checkStatusAndAddToChangeList(Collection<VirtualFile> files) throws VcsException {
    SimpleHandler ls;
    ls=new SimpleHandler(myProject, myVcsRoot, Command.LS);
    ls.setRemote(true);
    ls.endOptions();
    Collection<List<FilePath>> splitPaths=addPaths(ls, Util.virtualFileToFilePath(new ArrayList<VirtualFile>(files)));
    for(List<FilePath> paths:splitPaths) {
      //todo wc really need to clean up commands!
      //should be just ls.setPaths(paths);parseLsOutput(ls.run());
      ls=new SimpleHandler(myProject, myVcsRoot, Command.LS);
      ls.setRemote(true);
      ls.endOptions();
      ls.addRelativePaths(paths);
      parseLsOutput(ls.run());
    }
  }
  //todo wc move this into Handler
  private Collection<List<FilePath>> addPaths(Handler handler,List<FilePath> filePaths) {
    Collection<List<FilePath>> returnPaths=new HashSet<List<FilePath>>();
    if(handler.isAddedPathSizeTooGreat(filePaths)) {
      //cut the list size in half and try again !

      List<FilePath> firstHalfBigPaths=new ArrayList<FilePath>();
      Iterator<FilePath> iterator=filePaths.iterator();
      int i=0;
      while(++i<filePaths.size()/2+1) {
        firstHalfBigPaths.add(iterator.next());
        //iterator.remove();  //doesn't seem to work !?
      }
      filePaths.removeAll(firstHalfBigPaths);
      returnPaths.addAll(addPaths(handler,firstHalfBigPaths));
      returnPaths.addAll(addPaths(handler,filePaths));
    } else {
      if(filePaths.size() > 0)
        returnPaths.add(filePaths);
    }
    return returnPaths;
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
      //todo wc figure out what the hell is going on here..
        for (Change c : myChangeListManager.getChangesIn(myVcsRoot)) {
          if (c.getAfterRevision() != null) {
            addToPaths(rootPath, paths, c.getAfterRevision().getFile());
          }
          if (c.getBeforeRevision() != null) {
            addToPaths(rootPath, paths, c.getBeforeRevision().getFile());
          }
        }
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
    if (VcsUtil.getVcsRootFor(myProject,toAdd) != myVcsRoot) {
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
  private void collectVcsModifiedList() throws VcsException {
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

  private @NotNull Set<String> getFsWritableFiles() throws VcsException {
    Collection<FilePath> dirtyPaths = dirtyPaths(true);
    Set<String> writableFiles=new HashSet<String>();

    for(int i=0; i<MAX_THREADS; i++) {
      myThreads.add(new RecurseRunnable(writableFiles));
    }

    for(FilePath path:dirtyPaths) {
      recurseHijackedFiles(path.getIOFile(),writableFiles);
    }

    synchronized(myThreads) {
      while(true) { //wait until all threads have exited
        if(myThreads.size()==MAX_THREADS)
          break;
        try {
          myThreads.wait();
        } catch(InterruptedException e) {}
      }
    }

    return writableFiles;

    //Runtime.getRuntime().availableProcessors();
    //ApplicationManager.getApplication().executeOnPooledThread(); //see com.intellij.openapi.project.CacheUpdateRunner.getProcessWrapper
  }

  private VirtualFile createFileIfInRoot(String filename) throws VcsException {
    VirtualFile file=getVirtualFile(filename);
    return isInRoot(file) ? file : null;
  }

  private VirtualFile getVirtualFile(String filename) throws VcsException {
    VirtualFile file=myVcsRoot.findFileByRelativePath(Util.unescapePath(filename));
    if(file==null)
      file=myVcsRoot.findFileByRelativePath(
              Util.unescapePath(
                      Util.relativePath(myVcsRoot,VcsUtil.getFilePath(filename))));
    return file;
  }

  private boolean isInRoot(VirtualFile file) throws VcsException {
    return VcsUtil.getVcsRootFor(myProject,file) == myVcsRoot;
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

          File file=new File(filename);
          if(file != null) {
            String[] parts=line.substring(filenameEnd+filenameEndToken.length(), line.length()).split("\\s+",0);
            String relativeFilename=Util.relativePath(myVcsRoot,file);

            VirtualFile vfile=createFileIfInRoot(filename);
            if(vfile !=null && vfile.exists() && isInRoot(vfile)) {
              //this is a checked out file, which we'll automatically consider to be "modified"
              //in this case, the next string after "from" should be the version number that the checkout came from
              com.intellij.openapi.vcs.changes.ContentRevision before=
                      ContentRevision.createRevision(myVcsRoot,
                                                     relativeFilename,
                                                     new RevisionNumber(parts[0]),
                                                     myProject,
                                                     false,
                                                     true);
              com.intellij.openapi.vcs.changes.ContentRevision after=
                      ContentRevision.createRevision(myVcsRoot,
                                                     relativeFilename,
                                                     null,
                                                     myProject,
                                                     false,
                                                     true);
              myChanges.add(new Change(before, after, FileStatus.MODIFIED));
            } else { //it's a checked-out file that's been deleted
              com.intellij.openapi.vcs.changes.ContentRevision before=
                      ContentRevision.createRevision(myVcsRoot,
                                                     relativeFilename,
                                                     new RevisionNumber(parts[0]),
                                                     myProject,
                                                     false,
                                                     true);
              com.intellij.openapi.vcs.changes.ContentRevision after=
                      ContentRevision.createRevision(myVcsRoot,
                                                     relativeFilename,
                                                     null,
                                                     myProject,
                                                     false,
                                                     true);
              myChanges.add(new Change(before, after, FileStatus.DELETED));

            }
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
  private void recurseHijackedFiles(File file,Set<String> writableFiles) throws VcsException {
    //todo wc BEWARE LINKS THAT WILL CAUSE INFINITE RECURSION - OH NOES!
    VirtualFile vf=Util.stringToVirtualFile(myVcsRoot,Util.relativePath(myVcsRoot,file),true);
    if(vf!=null) {
    if(!ChangeListManager.getInstance(myProject).isIgnoredFile(vf) && !myFileIndex.isIgnored(vf)) {  //skip excluded files
      //if(file.isDirectory()) {
      File[] children=file.listFiles();
      if(children!=null) {  //implicit directory AND IO error check.
        for(File child:children)
          spawnOrRecurse(child, writableFiles);
      } else { //is a file
        //check if it's read-only
        //if yes, skip
        //if no, add to dirty list or add to changes right away
        if(file.canWrite()) {
          String relativeFilename=Util.relativePath(myVcsRoot,file);
          synchronized(writableFiles) {
            writableFiles.add(relativeFilename); //we don't know if it's been added or hijacked so don't put it in the change list yet, just take note
          }
        }
      }
    }
    } else { //probably a deleted file.
      synchronized(writableFiles) {  //we'll do our best to keep track of it...
        writableFiles.add(Util.relativePath(myVcsRoot,file));  //put it in the writeable files to be verified
      }
    }
  }
  private void spawnOrRecurse(File file,Set<String> writableFiles) {
    RecurseRunnable runner=null;
    synchronized(myThreads) {
      if(!myThreads.isEmpty())
        runner=myThreads.remove(myThreads.size()-1); //remove the last element instead of the first so that we don't have to recopy the array
    }

    try {
      if(runner == null) //there are no threads left.
        recurseHijackedFiles(file, writableFiles);
      else {
        runner.setFile(file);
        runner.run();
      }
    } catch(VcsException e) {
      myExceptions.add(e);
    }
  }

  private void endThreadRun(RecurseRunnable runner) {
    synchronized(myThreads) {
      myThreads.add(runner);
      myThreads.notify();
    }
  }
  private class RecurseRunnable implements Runnable {
    //Set<File> myIterableFiles;
    File myFile;
    Set<String> myWritableFiles;

    public RecurseRunnable(Set<String> writableFiles) {
      //myIterableFiles=new HashSet<File>();
      //myIterableFiles.add(file);
      myFile=null;
      myWritableFiles=writableFiles;
    }

    @Override
    public void run() {
      if(myFile == null) {
        throw new IllegalStateException("This object's file must be set prior to running");
      }

      try {
        recurseHijackedFiles(myFile,myWritableFiles);
      } catch(VcsException e) {
        myExceptions.add(e);
      }

      myFile=null;
      endThreadRun(this);
    }
    public void setFile(File file) {
      myFile=file;
    }
  }

}
