Transparent - making clearcase suck just a little bit less...

this is the vcs-api version

Usage
------
requires intellij build #662 or higher
copy transparent.jar to <intellij home>/plugins/
copy ccjni.dll to the bin directory
start intellij

In the project properties go to the VCS tab and select Clearcase.
Choose an implementation (Commandline or native)
Enter the root of the cc view

If it is a brand new project select Mark Project As Current in the Clearcase popup menu.

Work as usual. Checkout file when necessary but do not check in anything.
When you are ready to check in all files in your change set do a Check in Project (Ctrl-K).
The preview allows you to verify that only needed changes are going in. Right click to exclude changes.
Click ok to check in your changes. All add,delete,move,rename,modification will be committed to cc appropriately

Known limitations
-----------------
1) If you modify a file within IDEA and then modify it outside (like merging), it will not be included in the Check In Project.
   IDEA assumes "wrongly" that it is uptodate.
2) Files checked out but not modified are not unchecked out automatically as part of Project Check In.

Troubleshooting
---------------
Several log4j Categories are available to get information on the workings of the plugins:
   * net.sourceforge.transparent.TransparentVcs: plugin api trace
   * net.sourceforge.transparent.ClearCase: clearcase calls trace
   * net.sourceforge.transparent.actions.VcsAction: plugin actions trace
   * net.sourceforge.transparent.CommandLineClearCase: cli command trace

Modify IDEA_DIR/bin/log.xml to append the following tags:
1) This turns everything on
  <category name="net.sourceforge.transparent">
    <priority value="DEBUG"/>
    <appender-ref ref="CONSOLE-DEBUG"/>
  </category>

2) This turns only the plugin actions trace
  <category name="net.sourceforge.transparent.actions.VcsAction">
    <priority value="DEBUG"/>
    <appender-ref ref="CONSOLE-DEBUG"/>
  </category>

Change history
--------------

---++++ 1.27 (963 only) - 07 Nov 03

   * Migrated to 963

---++++ 1.26 (957 only) - 23 Oct 03

   * Add action brings up the checkin dialog
   * Fix error message on clearcase action on unix due to wrong handling of quoted comments (reported by Rick Maddy)
   * Fix CommandLineClearcase directory addition misusing "cleartool mkdir" (reported by Rick Maddy)
   * Migrated to 957

---++++ 1.25 (944 only) - 2 Oct 03

   * Fix NPE when bringing up the VCS menu and Clearcase plugin is not the selected vcs
   * Fix NPE when file deleted
   * Fix Editing a file in a zip or jar file triggering the hijack/checkout dialog
   * Fix JVM crash if using offline mode with NativeClearcase and clearcase is not available
   * Fix assertion when readonly private file is modified (it now hijacks it)

---++++ 1.24 (915 only) - 3 Sep 03

   * Fix NPE in automatic checkout of readonly file on edit
   * Fix NPE in excluding deleted file
   * Fix inconsistent action/menu names
   * Fix move/rename/delete/add not using the current check in comment
   * Fix adding directory in the CommandLine implementation

---+++++ Warning

   * I had some race-condition with the NewCommandLineClearcase implementation so I am pulling it out until I can fix it

---++++ 1.23 (915 only) - 1 Sep 03

   * Fix Hijacking file not in write action
   * Fix text erroneously asking to Hijack when it will Check out on the dialog that pop up on edit of a read-only file
   * Fix offline mode not working for rename/move
   * Fix failed attempt to modify readonly file on edit not reported in GUI
   * Make Check Out File action turn into Hijack File when working in offline mode
   * New CommandLine interface using long-lived cleartool process for 2x performance improvement
   * New Native interface that use the COM wrapper "jacob" (http://danadler.com/jacob/) instead of custom coded C++

---+++++ Warning

   * If you are using the new Native implementation please note that a new dll "jacob.dll" needs to be put in IDEA_DIR/bin
   * If you are tired of doing this please vote for http://www.intellij.net/tracker/idea/viewSCR?publicId=5769

---++++ 1.22 (896 only) - 22 Aug 03

   * Upgraded to 896! FINALLY ;-)
   * Add new option to checkout a readonly file automatically on modification without asking.
   * Add option to excluded paths/files
   * Add a offline mode that won't even check out files on edit but hijack them instead. On check in the file will be automatically checked out (see next option)
   * Add option to use hijack on check in (hijacked files will be first checked out then checked in)
   * Checkin project bring up a dialog instead of the error panel to signify there is nothing to check in
   * Checkout no longer report an error if file is already checked out
   * Fix readonly modification listener (Checkout dialog) always active even if clearcase is not the active vcs
   * Fix on Aurora move,rename would do an delete/add instead of mv

---+++++ Known limitations

   * Due to a bug in IDEA, never move a file and delete the directory from which it was moved in the same transaction (Check In Project). The file will end up being view private in the new location and delete in the old (adding the file back in manually will loose the history). [[http://www.intellij.net/tracker/idea/viewSCR?publicId=15174][Vote]] for the bug!
   * Moving file in/out of excluded paths is not handled properly:
      1 moving out of excluded paths will not add automatically the file
      2 moving in excluded paths will add the directories up to the moved file
   * Excluded paths are not relative to the project file yet.

---++++ 1.21 (813 and 3.0) - 7 May 03

   * Fix Check in mapping problem
   * Fix NPE when switching from and to CVS VCS and invoking the VCS tool submenu
   * Fix broken support for rename/move broken in 1.20
   * Fix broken move of file to a new directory not added already
   * Fix move of file where old name/location is being used already (move src/a/A src/b/A, add src/a/A, check in project)
   * Fix rename of file where old name/location is being used already (rename src/a/A src/a/B, add src/a/A, check in project)
   * Put more error reporting on Delete/Move/Rename 
   * Make "Mark File As Up-to-date" always enabled
   * Move now check that target parent is not an element. If it isn't it will add the whole path in.
   * Remove the Clearcase status check to appropriately enable actions (TOO SLOW!!!). Actions are back to be enabled based on IDEA state
   * Add back the Add action and make it always available (cheap way to handle recursive adds since querying for the state of all files under a directory would be SLOOOOOOW)

---+++ 1.20 (813 and 3.0) - 24 Apr 03

   * Update file cannot be recursive anymore
   * Fixed "OK to All" button on Check In to reset at end of transaction
   * Added "Mark File as Up To Date" option on popup in Check In Project Right Click Options
   * Added Scr Field to Check In and Check In Project.  Scr Field is saved to a text file (location is configurable in the properties) for a trigger to pick up.
   * Comments are no longer required but if entered cannot exceed 1 line
   * Added popup box when trying to Check-In a non-modified file.
   * Made ScrField and CommentArea highlight on focus.
   * Change Add File function to maintain comment on directory and on file.
   * Changed All Error Messages to the standard VCS Error Handling. NO MORE scary but benign "General application error, please restart IDEA" popup.
   * Check Out, Check In, Check In Project, and Undo Check Out when ran, properly report files on which the action is inappropriate. The operation will still apply to the files in the correct state.
   * Fix delete of a file when a move/rename of another file to the same location happens in the same transaction.
   * When file is hijacked and has been changed, and the user checks it out, a pop-up box asks whether the user wants to keep the hijacked as the checked out file or not.
   * Fix not working deletion of directory (StackOverflow and problem with directory content deletion)
   * Turn off transaction so that incomplete transactions are up-to-date in IDEA
   * Move,rename,delete,add will use the check in project comment for directory check in

---++++ 1.18 (#668) - 30 Oct 02

   * Upgraded to 668 but SHOULD be backward compatible with 666
   * Fixed Version Tree & History not working with hijacked files
   * Added a build time stamp in the about dialog
   * Add, Check in/out, Undo Checkout, Update File, Mark File as uptodate can be recursive
   * Added Comment on individual file check in

---++++ 1.17 (#666) - 29 Oct 2002

   * Refactor logging to use log4j Loggers: See ClearcasePluginManual#Troubleshooting
   * Added option to not mark external changes as up-to-date in IDEA (Good for running findmerge/merge/update)
   * Add action Mark File As Up-To-Date in order to permanently omit the file for the next Check In Project

---++++ 1.16 (#662) - 21 Oct 2002

   * Fix reserved checkout problem with the CommandLineClearcase
   * Fix checkin file crash with NativeClearcase
   * Misc actions can be invoked in parallel (use CLI instead of the native)

---++++ 1.15 -  9 Oct 2002

   * Changed Update action so it brings up the gui like Update Project does
   * fix ClassCastException in misc actions when an clearcase error occurs

---++++ 1.14 - 1 Oct 2002

   * Fix Check for availability of actions based on the VCS status (not CC status because of performance issues)

---++++ 1.13 - 1 Oct 2002

   * Fix About action
   * Fix Check In Project action
   * Add Merge Project action (use the Merge Manager)
   * Known issue: Check in a single file doesn't query for comment

---++++ 1.12 - 26 Sept 2002

   * Upgraded to 650

---++++ 1.11 - 25 Sept 2002

   * Option to do reserved checkouts (Directories are still checked out unreserved)
   * Ability to enter check in comments (On "Check In Project" action, the same comment is used for all files checked in).

---++++ 1.10 - 18 Sept 2002

   * Miscellaneous actions do not lock IDEA anymore. However if the native mode is selected only one external CC tool can be run at any time. Other requests are queued (JNI limitation).
   * Checkout/uncheckout file only refresh that file and not the entire project
   * Added about box with version

---++++ 1.9 - 12 Sept 2002

   * Add Find checkouts, Update project, Find Project checkouts,
   * Fix update action
   * Check for availability of actions based on the VCS status (not CC status because of performance issues)

---++++ 1.7 - 7 Sept 2002

   * Fix multiple file actions (check out, uncheckout). Can now select the top level nodes in refactoring previews and invoke Checkout action to check all files out in one shot!

---++++ 1.5 - 6 Sept 2002

   * Graceful handling of error: a dialog pops up when clearcase aborts explaining the error

---++++ 1.4 - 5 Sept 2002

   * Recompiled for build 644.
   * Fix refresh problem on automatic checkout of readonly files
   * Fix the add file that leaves the parent checked out
   * Fix move/rename that leaves the moved/renamed file not controlled
   * Add checkout action

---++++ 1.3 - 21 Aug 2002

   * Recompiled for build 641. No changes

---++++ 1.2 - 6 Aug 2002

   * Update for build 639. The plugin won't work for earlier build anymore
   * Fix bug that left some files and directories checked out
   * Clean up tests

---++++ 1.1 - 29 Jul 2002

   * Update for build 638. The plugin won't work for earlier build anymore
   * Optional checkout of readonly files on modification

---++++ 1.0 - 24 Jul 2002

   * Baseline for build 632.
   * Support native mode on Windows
   * Full OpenAPI-VCS implementation.


Known Bugs
----------
   * Move a file and delete the directory from which it was moved in the same transaction (Check In Project) is badly broken:
      workaround 1: move the file, check in project then delete directory and check in project again.
      workaround 2: move the file and delete directory. check in project once and exclude deleted directory
                    then check in project again but include the deleted directory this time
   * Rename files from upper to lowercase or vice-versa doesn't work.
   * Move from a directory that has been removed will recreate locally the directory (not in clearcase)

Feature requests
----------------
1.  Undo Hijacked File
2.  Differences Page
3.  Refresh Clearcase Status into IntelliJ
