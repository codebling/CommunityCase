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

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.community.intellij.plugins.communitycase.ContentRevision;
import org.community.intellij.plugins.communitycase.Util;
import org.community.intellij.plugins.communitycase.Vcs;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.Handler;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.community.intellij.plugins.communitycase.config.VcsSettings;
import org.community.intellij.plugins.communitycase.history.HistoryUtils;
import org.community.intellij.plugins.communitycase.i18n.Bundle;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

//todo wc clean up this shitty shitty code
//todo wc changes requested once for each module, with the module as myVcsRoot. Account for this and make sure we're compatible
/**
 * A collector for changes in version control. It is introduced because changes are not
 * cannot be got as a sum of stateless operations.
 *
 ===================
 ct lsco -a -me -cvi
 5 seconds bamain from anywhere in tree

 format:
 --12-21T17:09  ascher     checkout version "C:\cc\baplugintest\serverdev\lost+found\wrapper_32.6223c92a584d4266bcc1b081259b9b2c" from \main\0 (reserved)
   "CHECKOUT COMMENT!!!"
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

  private static final Logger log=Logger.getInstance("#"+ChangeCollector.class.getName());
  private final Project myProject;
  private final ChangeListManager myChangeListManager;
  //private final ProgressIndicator myProgressIndicator;
  private final VcsDirtyScope myDirtyScope;

  private final ProjectFileIndex myFileIndex;

  private final VirtualFile myRoot; //will be one of the dirty paths
  private final List<VirtualFile> myUnversioned = new ArrayList<VirtualFile>(); // Unversioned files
  private final List<Change> myChanges = new ArrayList<Change>(); // all changes
  private boolean myIsCollected = false; // indicates that collecting changes has been started

  private static final int MAX_THREADS=15;
  private final List<RecurseRunnable> myRecurseThreads=new ArrayList<RecurseRunnable>();
  private final List<LsRunnable> myLsThreads=new ArrayList<LsRunnable>();

  private Pattern myPathFilter=null;

  public ChangeCollector(final Project project,
                         ChangeListManager changeListManager,
                         final ProgressIndicator progressIndicator,
                         VcsDirtyScope dirtyScope,
                         final VirtualFile root) {
    myChangeListManager = changeListManager;
    //myProgressIndicator = progressIndicator;
    myDirtyScope = dirtyScope;
    myRoot=root;
    myProject = project;
    myFileIndex=ProjectRootManager.getInstance(myProject).getFileIndex();
  }

  /**
   * Get unversioned files
   * @return the changes
   * @throws com.intellij.openapi.vcs.VcsException in several cases
   */
  public Collection<VirtualFile> unversioned() throws VcsException {
    ensureCollected();
    return myUnversioned;
  }

  /**
   * Get changes
   * @throws com.intellij.openapi.vcs.VcsException in several cases
   * @return the changes
   */
  public Collection<Change> changes() throws VcsException {
    ensureCollected();
    return myChanges;
  }


  /**
   * Ensure that changes has been collected.
   * @throws com.intellij.openapi.vcs.VcsException in several cases
   */
  private void ensureCollected() throws VcsException {
    if(!myIsCollected) {
      myIsCollected = true;

      for(int i=0; i<MAX_THREADS; i++) {
        myLsThreads.add(new LsRunnable());
      }

      if(VcsSettings.getInstance(myProject)!=null && !VcsSettings.getInstance(myProject).getPathFilter().isEmpty()) {
        try {
          //disable the inspection, we check if null above.
          //noinspection ConstantConditions
          myPathFilter=Pattern.compile(VcsSettings.getInstance(myProject).getPathFilter());
        } catch(Exception e) {
          throw new VcsException(Bundle.getString("vcs.config.pathfilter.badregex"),e);
        }
      }
      collectVcsModifiedList();

      if(!DumbService.getInstance(myProject).isDumb()) {  //don't go to town on the HD if we're indexing, causes excessive thrashing
        Map<String,VirtualFile> vfMap=Util.stringToVirtualFile(myRoot,
                                                               getFsWritableFiles(),
                                                               true);

        for(Iterator<Map.Entry<String,VirtualFile>> i=vfMap.entrySet().iterator(); i.hasNext();) {
          Map.Entry<String,VirtualFile> pair=i.next();
          if(pair.getValue() == null) {
            popupNotification(NotificationType.WARNING, Bundle.message("changes.ls.mappingerr.content", pair.getKey()));
            i.remove();
          }
        }

        Collection<VirtualFile> addedOrHijackedOrCheckedOutFiles=vfMap.values();

        //we already have all the info we need about checked out files, so remove those from the list
        for(Change change:myChanges)
          addedOrHijackedOrCheckedOutFiles.remove(change.getVirtualFile()); //remove files already in the checkout list. We don't need to check the status; we already know it.
                                                                            //VirtualFile has no equals method, but since it is unique for this
                                                                            //IntelliJ aka VM instance, we're ok to use Object's default one with the Set (Sets compare elements for equality)
        chunkCheckAdd(addedOrHijackedOrCheckedOutFiles);
        synchronized(myLsThreads) {
          while(true) { //wait until all threads have exited
            if(myLsThreads.size()==MAX_THREADS)
              break;
            try {
              myLsThreads.wait();
            } catch(InterruptedException ignored) {}
          }
        }
        myLsThreads.clear();
      } else {  //if we're in dumb mode, trigger a refresh after all files are indexed
        //todo wc don't register this listener several times! Put a variable in ChangeProvider that can remember if there's one installed.
        myProject.getMessageBus().connect().subscribe(DumbService.DUMB_MODE, new DumbService.DumbModeListener() {
          public void enteredDumbMode() {}
          public void exitDumbMode() {
            //VcsUtil.runVcsProcessWithProgress()
            HashSet<FilePath> paths=new HashSet<FilePath>();
            paths.add(Util.virtualFileToFilePath(myRoot));
            VcsUtil.refreshFiles(myProject, paths);
          }
        });
      }
    }
  }

/*
  private Collection<VirtualFile> getProjectPaneFiles() {
    HashSet<VirtualFile> accumulator=new HashSet<VirtualFile>();

    ProjectView pv=ProjectView.getInstance(myProject);
    DefaultMutableTreeNode rootNode=pv.getProjectViewPaneById("ProjectPane").getTreeBuilder().getRootNode();
    recurseTreeAndAddFiles(accumulator,rootNode);
    return accumulator;
  }
  private void recurseTreeAndAddFiles(HashSet<VirtualFile> accumulator,@NotNull DefaultMutableTreeNode node) {
    Object userObject=node.getUserObject();
    if(userObject !=null && userObject instanceof PsiFileSystemItem)
      accumulator.add(((PsiFileSystemItem)userObject).getVirtualFile());
    Enumeration<DefaultMutableTreeNode> i=node.children();
    while(i.hasMoreElements()) {
      recurseTreeAndAddFiles(accumulator,i.nextElement());
    }
  }
*/
/*
  private void collectProjectFiles() {
    ProjectView pv=ProjectView.getInstance(myProject);
    DefaultMutableTreeNode rootNode=pv.getProjectViewPaneById("ProjectPane").getTreeBuilder().getRootNode();
    recurseTreeAndAddFiles(rootNode);
  }
  private void recurseTreeAndAddFiles(@NotNull DefaultMutableTreeNode node) {
    Object userObject=node.getUserObject();
    if(userObject !=null && userObject instanceof PsiFileSystemItem) {
      if(userObject instanceof PsiDirectory)
        myProjectPaneDirs.add(((PsiFileSystemItem)userObject).getVirtualFile());
      else if(userObject instanceof PsiFile)
        myProjectPaneFiles.add(((PsiFileSystemItem)userObject).getVirtualFile());
    }
    Enumeration<DefaultMutableTreeNode> i=node.children();
    while(i.hasMoreElements()) {
      recurseTreeAndAddFiles(i.nextElement());
    }
  }

*/
  private void chunkCheckAdd(Collection<VirtualFile> files) throws VcsException {
    SimpleHandler ls;
    ls=new SimpleHandler(myProject, myRoot, Command.LS);
    ls.setRemote(true);
    ls.endOptions();
    Collection<List<FilePath>> splitPaths=addPaths(ls, Util.virtualFileToFilePath(new ArrayList<VirtualFile>(files)));
    for(List<FilePath> paths:splitPaths)
      spawnLs(paths);
  }
  private void checkStatusAndAddToChangeList(Collection<FilePath> paths) throws VcsException {
    SimpleHandler ls=new SimpleHandler(myProject, myRoot, Command.LS);
    ls.setRemote(true);
    ls.endOptions();
    ls.addRelativePaths(paths);
    parseLsOutput(ls.run());
  }

  //todo wc move this into Handler
  //todo wc this is NOT efficient. chunk to the right size instead.
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
    FilePath rootPath = VcsUtil.getFilePath(myRoot.getPath(), true);
    for (FilePath p : myDirtyScope.getRecursivelyDirtyDirectories()) {
      addToPaths(rootPath, paths, p);
    }
    ArrayList<FilePath> candidatePaths = new ArrayList<FilePath>();
    candidatePaths.addAll(myDirtyScope.getDirtyFilesNoExpand());
    if (includeChanges) {
      //todo wc figure out what the hell is going on here..
        for (Change c : myChangeListManager.getChangesIn(myRoot)) {
          if (c.getAfterRevision() != null) {
            //noinspection ConstantConditions
            addToPaths(rootPath, paths, c.getAfterRevision().getFile());
          }
          if (c.getBeforeRevision() != null) {
            //noinspection ConstantConditions
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
   * Takes the list of all directory (not .jar or .zip) roots for all modules and determines
   * if any of the passed path are predecessors/ancestors of them.
   * If so, adds it to the list of paths which will be returned.
   * @param paths the paths to expand
   * @return a list of all module source paths for which one of the passed paths is an ancestor
   */
  private Collection<FilePath> expandPathsToRoots(Collection<FilePath> paths) {
    Collection<FilePath> expanded=new HashSet<FilePath>();
    for(Module m : ModuleManager.getInstance(myProject).getModules())
      for(VirtualFile v : OrderEnumerator.orderEntries(m).getSourcePathsList().getVirtualFiles())
        for(FilePath p:paths)
          if(v.isDirectory() && v.getPath().startsWith(p.getPath())) //check if is directory (sometimes .zip or .jar are returned) and that it has our dirtypath as one of its ancestors
            expanded.add(Util.virtualFileToFilePath(v));
    return expanded;
  }

  /**
   * Takes the list of all directory (not .jar or .zip) roots for all modules and determines
   * if any of the passed path are predecessors/ancestors of them.
   * If so, adds it to the list of paths which will be returned.
   * @param paths the paths to expand
   * @return a list of all module source paths for which one of the passed paths is an ancestor
   */
  private Collection<FilePath> expandPathsToModuleBases(Collection<FilePath> paths) {
    Collection<FilePath> expanded=new HashSet<FilePath>();
    Set<FilePath> pathsCopy=new HashSet<FilePath>(paths);
    List<VirtualFile> moduleContentRoots=new ArrayList<VirtualFile>();

    for(Module m:ModuleManager.getInstance(myProject).getModules())
      moduleContentRoots.addAll(Arrays.asList(ModuleRootManager.getInstance(m).getContentRoots()));

    pathLoop:
    for(Iterator<FilePath> pathIterator=pathsCopy.iterator();pathIterator.hasNext();) {
      FilePath path=pathIterator.next();

      for(Iterator<VirtualFile> mcrIterator=moduleContentRoots.iterator();mcrIterator.hasNext();) {
        VirtualFile mcr=mcrIterator.next();

        if(mcr.isDirectory()) {
          if(mcr.getPath().startsWith(path.getPath())) { //check if is directory (sometimes .zip or .jar are returned) and that it has our dirtypath as one of its ancestors
            expanded.add(Util.virtualFileToFilePath(mcr));
            mcrIterator.remove(); //only add once (& don't bother iterating through this in the future)
          } else if(path.getPath().startsWith(mcr.getPath())) { //or path.isUnder(mcr,false) i.e. if the path is UNDER the module, only add the path
            expanded.add(path);
            pathIterator.remove();
            continue pathLoop;
          }
        }
      }
    }
    return expanded;
  }

  /**
   * Add path to the collection of the paths to check for this vcs root
   *
   * @param root  the root path
   * @param paths the existing paths
   * @param toAdd the path to add
   */
  void addToPaths(FilePath root, Collection<FilePath> paths, FilePath toAdd) {
    if (VcsUtil.getVcsRootFor(myProject,toAdd) !=myRoot) {
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
//    HandlerUtil.runInCurrentThread(ls, myProgressIndicator, false, "VCS refresh");
//    HandlerUtil.doSynchronously(ls, "VCS refresh", "VCS refresh");

    SimpleHandler lsco= new SimpleHandler(myProject, myRoot, Command.LS_CHECKOUTS);
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
      myRecurseThreads.add(new RecurseRunnable(writableFiles));
    }
    //testSetup();
    for(FilePath path:expandPathsToModuleBases(dirtyPaths))
      spawnOrRecurse(path.getIOFile(),writableFiles, -1);

    VirtualFile projBaseDir=myProject.getBaseDir();
    if(projBaseDir!=null && isInRoot(projBaseDir))
      spawnOrRecurse(Util.virtualFileToFile(projBaseDir),writableFiles,1); //recurse only the base directory, no children
    synchronized(myRecurseThreads) {
      while(true) { //wait until all threads have exited
        if(myRecurseThreads.size()==MAX_THREADS)
          break;
        try {
          myRecurseThreads.wait();
        } catch(InterruptedException ignored) {}
      }
    }
    myRecurseThreads.clear();
    return writableFiles;
  }

  private VirtualFile createFileIfInRoot(String filename) throws VcsException {
    VirtualFile file=getVirtualFile(filename);
    return isInRoot(file) ? file : null;
  }

  private VirtualFile getVirtualFile(String filename) throws VcsException {
    VirtualFile file=myRoot.findFileByRelativePath(Util.unescapePath(filename));
    if(file==null)
      file=myRoot.findFileByRelativePath(
              Util.unescapePath(
                      Util.relativePath(myRoot,VcsUtil.getFilePath(filename))));
    return file;
  }

  private boolean isInRoot(VirtualFile file) throws VcsException {
    return VcsUtil.getVcsRootFor(myProject,file) ==myRoot;
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
   * @param list the output from the lsco command
   * @throws VcsException in several cases
   */
  private void parseLsCheckoutsOutput(String list) throws VcsException {
    //Line format:
//--12-21T17:09  ascher     checkout version "C:\cc\baplugintest\serverdev\lost+found\wrapper_32.6223c92a584d4266bcc1b081259b9b2c" from \main\0 (reserved)
//--02-25T18:12  ascher     checkout directory version "C:\cc\bamain\serverdev\server\mailgtw\mmt\cache\logic\_src\com\oz\mailgtw\mmt\cache\logic\billing\factory" from \main\2 (unreserved)
    BufferedReader reader=new BufferedReader(new StringReader(list));

    //final String filenameStartToken="checkout version \"";
    final String filenameStartToken="checkout (directory )?version \"";
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
        String[] splitOnToken=line.split(filenameStartToken);
        if(splitOnToken.length==2) {
          int filenameEnd=splitOnToken[1].lastIndexOf(filenameEndToken);
          if(filenameEnd>0) {
            String filename=splitOnToken[1].substring(0,filenameEnd);

            File file=new File(filename);
            String[] parts=splitOnToken[1].substring(filename.length()+filenameEndToken.length(),splitOnToken[1].length()).split("\\s+",0);
            String relativeFilename=Util.relativePath(myRoot,file);

            VirtualFile vfile=getVirtualFile(filename);
            if(vfile !=null && vfile.exists() && isInRoot(vfile)) {
              //this is a checked out file, which we'll automatically consider to be "modified"
              //in this case, the next string after "from" should be the version number that the checkout came from
              com.intellij.openapi.vcs.changes.ContentRevision before=
                      ContentRevision.createRevision(myRoot,
                                                     relativeFilename,
                                                     HistoryUtils.createUnvalidatedRevisionNumber(parts[0]),
                                                     myProject,
                                                     false,
                                                     true);
              com.intellij.openapi.vcs.changes.ContentRevision after=
                      ContentRevision.createRevision(myRoot,
                                                     relativeFilename,
                                                     null,
                                                     myProject,
                                                     false,
                                                     true);
              if(!file.isDirectory() || VcsSettings.getInstance(myProject).isShowDirectories())
                myChanges.add(new Change(before, after, FileStatus.MODIFIED));
              //else  //appears in yellow as file to be added
              //  myChanges.add(new Change(before, after, FileStatus.IGNORED));
            } else { //it's a checked-out file that's been deleted
              //todo wc if the file is deleted but isn't in the root, do not add to list of changes
              com.intellij.openapi.vcs.changes.ContentRevision before=
                      ContentRevision.createRevision(myRoot,
                                                     relativeFilename,
                                                     HistoryUtils.createUnvalidatedRevisionNumber(parts[0]),
                                                     myProject,
                                                     false,
                                                     true);
              com.intellij.openapi.vcs.changes.ContentRevision after=
                      ContentRevision.createRevision(myRoot,
                                                     relativeFilename,
                                                     null,
                                                     myProject,
                                                     false,
                                                     true);
              //todo wc if it's a deleted file, we won't actually know if it's a directory or not so it will still be shown.
              if(!file.isDirectory() || VcsSettings.getInstance(myProject).isShowDirectories())
                myChanges.add(new Change(before, after, FileStatus.DELETED));
              //else
              //  myChanges.add(new Change(before, after, FileStatus.IGNORED));
            }
          }
        }
      }
    }
  }

  /**
   * Parse Command.LS output.
   * @param list the output from the ls command
   * @throws VcsException in several cases
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
      //noinspection UnusedAssignment
      line=null;
      //noinspection UnusedAssignment
      versionStartIndex=-2; //bogus value which will be overwritten
      //noinspection UnusedAssignment
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
                String relativeFilename=Util.relativePath(myRoot,file);
                if(parts[1].equals("from")) {
                  //this is a checked out file, which we'll automatically consider to be "modified"
                  //in this case, the next string after "from" should be the version number that the checkout came from
                  com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myRoot,relativeFilename,HistoryUtils.createUnvalidatedRevisionNumber(parts[2]),myProject,false,true);
                  //com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myRoot, relativeFilename, new VcsRevisionNumber(version), myProject, false, true);
                  com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myRoot,relativeFilename,null,myProject,false,true);
                  myChanges.add(new Change(before, after, FileStatus.MODIFIED));
                } else {
                  if(parts[1].equals("[hijacked]")) {
                    //wrapper_diameter_loopback.conf.375035980431452ba39e23f44acd7567@@\main\0 [hijacked]      Rule: \main\LATEST
                    com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myRoot, relativeFilename,HistoryUtils.createUnvalidatedRevisionNumber(version), myProject, false, true);
                    com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myRoot, relativeFilename, null, myProject, false, true);
                    myChanges.add(new Change(before, after, FileStatus.HIJACKED));
                  }
                }
              } else {
                if(parts.length >= 4 && parts[1].equals("[loaded") && parts[2].equals("but") && parts[3].equals("missing]")) {
                  //wrapper_yahooservices_out.conf.72bc32ce8b4a417b9961dda95d7799bf@@\main\0 [not loaded]    Rule: \main\LATEST
                  com.intellij.openapi.vcs.changes.ContentRevision before=ContentRevision.createRevision(myRoot, filename,HistoryUtils.createUnvalidatedRevisionNumber(version), myProject, false, true);
                  com.intellij.openapi.vcs.changes.ContentRevision after=ContentRevision.createRevision(myRoot, filename, null, myProject, true, true);
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
  /*
  //for testing methods of ignoring files
  Set<VirtualFile> myTestFiles = new HashSet<VirtualFile>();
  private void testSetup() {
    addChildren(new File("C:\\cc\\bamain\\serverdev\\server"),false,false);
    addChildren(new File("C:\\cc\\bamain\\serverdev\\server"),true,false);
    addChildren(new File("C:\\cc\\bamain\\serverdev\\server\\mailgtw"),true,false);
  }
  private void addChildren(File file,boolean children,boolean onlyDirectories) {
    if(children)
      for(File f:file.listFiles())
        addFile(f,onlyDirectories);
    else
      addFile(file,onlyDirectories);
  }
  private void addFile(File file,boolean onlyDirectory) {
    try {
      VirtualFile v=Util.fileToVirtualFile(myRoot,file,true);
      if(v!=null && (!onlyDirectory || v.isDirectory()) )
        myTestFiles.add(v);
    } catch(VcsException e) {}
  }
  */
  private void recurseHijackedFiles(File file, Set<String> writableFiles, int maxDepth) throws VcsException {
    //todo wc BEWARE LINKS THAT WILL CAUSE INFINITE RECURSION - OH NOES!
    VirtualFile vf=Util.stringToVirtualFile(myRoot,Util.relativePath(myRoot,file),true);
    if(vf!=null) {
      //skip excluded files

      /* these are always all false
      //for testing ways of finding excluded files
      //if(myTestFiles.contains(vf)) {
      if(maxDepth==0) {
        System.out.println(vf);
        System.out.println("\tprojectContainsFile(proj,file,islib=false) \t"+ModuleUtil.projectContainsFile(myProject, vf, false));
        System.out.println("\tprojectContainsFile(proj,file,islib=true) \t"+ModuleUtil.projectContainsFile(myProject,vf,true));
        System.out.println("\tisProjectExcludeRoot \t\t"+DirectoryIndex.getInstance(myProject).isProjectExcludeRoot(vf));
        System.out.println("\tmyFileIndex.isIgnored \t\t"+myFileIndex.isIgnored(vf));
        System.out.println("\tChangeListManager.isIgnoredFile \t\t"+ChangeListManager.getInstance(myProject).isIgnoredFile(vf));
      }
      */
      //if(/*ModuleUtil.projectContainsFile(myProject,vf,false) &&
       if(!ChangeListManager.getInstance(myProject).isIgnoredFile(vf)
              && !myFileIndex.isIgnored(vf)
              && (myPathFilter==null
                  || !myPathFilter.matcher(vf.getPath()).find() )) { //if this line is removed, we must find a way to ignore .keep and .contrib
        //if(file.isDirectory()) { //not needed, implicit check below
        File[] children=file.listFiles();
        if(children!=null) {  //implicit directory AND IO error check.
          if(maxDepth!=0)
            for(File child:children)
                spawnOrRecurse(child, writableFiles, maxDepth>0? --maxDepth:maxDepth); //if maxDepth is negative, don't subtract first.
        } else { //is a file
          //check if it's read-only
          //if yes, skip
          //if no, add to dirty list or add to changes right away
          if(file.canWrite() && vf.getExtension()!=null) {
            String relativeFilename=Util.relativePath(myRoot,file);
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized(writableFiles) {
              writableFiles.add(relativeFilename); //we don't know if it's been added or hijacked so don't put it in the change list yet, just take note
            }
          }
        }
      }
    } else { //probably a deleted file.
      //noinspection SynchronizationOnLocalVariableOrMethodParameter
      synchronized(writableFiles) {  //we'll do our best to keep track of it...
        writableFiles.add(Util.relativePath(myRoot,file));  //put it in the writeable files to be verified
      }
    }
  }

  private void spawnOrRecurse(File file, Set<String> writableFiles, int maxDepth) throws VcsException {
    RecurseRunnable runner=null;
    synchronized(myRecurseThreads) {
      if(!myRecurseThreads.isEmpty())
        runner=myRecurseThreads.remove(myRecurseThreads.size()-1); //remove the last element instead of the first so that we don't have to recopy the array
    }

    if(runner == null) //there are no threads left.
      recurseHijackedFiles(file, writableFiles, maxDepth);
    else {
      runner.setFile(file);
      runner.setMaxDepth(maxDepth);
      new Thread(runner).start(); //runner.run();
    }
  }

  private void endThreadRun(RecurseRunnable runner) {
    synchronized(myRecurseThreads) {
      myRecurseThreads.add(runner);
      myRecurseThreads.notify();
    }
  }
  private void endThreadRun(LsRunnable runner) {
    synchronized(myLsThreads) {
      myLsThreads.add(runner);
      myLsThreads.notify();
    }
  }
  private void spawnLs(Collection<FilePath> files) throws VcsException {

    LsRunnable runner=null;
    synchronized(myLsThreads) {
      if(!myLsThreads.isEmpty())
        runner=myLsThreads.remove(myLsThreads.size()-1); //remove the last element instead of the first so that we don't have to recopy the array
    }

    if(runner == null) //there are no threads left.
      checkStatusAndAddToChangeList(files);
    else {
      runner.setFiles(files);
      new Thread(runner).start(); //runner.run();
    }
  }

  private class RecurseRunnable implements Runnable {
    //Set<File> myIterableFiles;
    File myFile;
    Set<String> myWritableFiles;
    private int myMaxDepth=0;

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
        recurseHijackedFiles(myFile,myWritableFiles,myMaxDepth);
      } catch(VcsException e) {
        popupNotification(e);
      }

      myFile=null;
      endThreadRun(this);
    }
    public void setFile(File file) {
      myFile=file;
    }

    public void setMaxDepth(int maxDepth) {
      myMaxDepth=maxDepth;
    }
  }
  private class LsRunnable implements Runnable {
    Collection<FilePath> myFiles;

    public void setFiles(Collection<FilePath> files) {
      myFiles=files;
    }

    @Override
    public void run() {
      if(myFiles == null) {
        throw new IllegalStateException("This object's file must be set prior to running");
      }

      try {
        checkStatusAndAddToChangeList(myFiles);
      } catch(VcsException e) {
        popupNotification(e);
      }

      myFiles=null;
      endThreadRun(this);
    }
  }

  private void popupNotification(Throwable e) {
    String message=Bundle.message("changes.err.content")+e.getMessage();
    //popupNotification(NotificationType.ERROR,message);
    log.error(message,e);
  }
  private void popupNotification(NotificationType type,String s) {
    Notifications.Bus.notify(
      new Notification(Vcs.NOTIFICATION_GROUP_ID,Bundle.message("changes.err.title"),s,type),
      myProject);
  }
}
