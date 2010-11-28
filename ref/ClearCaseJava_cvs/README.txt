README
======
 
 History:
 2009-01-06 mike adding info and restructure.
 2008-01-17 mike updating instructions.
 



Description
===========

ClearCase java API to be used as an layer between clearcase and java code.
It enables the java code to interact with clearcase.

Usage
=====

* There is a sample application included in the jar file. 

Prerequiste
-----------

The vob-path in my example is:

M:\mike_clearcase\myapp\test>   (Win)

/vobs/test/myapp/test (Unix/Linux)

ClearCase client for Win/Unix/Linux installed. Make sure that the cleartool.exe (Win)
or cleartool (Unix/Linux) is in the PATH. Try by typing:

M:\mike_clearcase\myapp\test>cleartool (Win)

and the cleartool-prompt will show.

cleartool>             


Windows
-------

M:\mike_clearcase\myapp\test>java -jar clearcase-java.jar

* Will give you:

Number of args :0
Usage: java -jar clearcase-java.jar command [option]
Command - checkout,checkin,delete,create_dir, create_file,configspec,vobnames,viewnames,list
Option - *, element, list of elements, none

Example 1:

M:\test\myapp\test>java -jar clearcase-java.jar create_dir testdir1
Number of args :2
Command : create_dir
Options :
testdir1
Started ...
2008-jan-17 10:47:53 net.sourceforge.clearcase.ClearCaseCLIImpl launch
FIN: launching: cleartool mkdir -nc testdir1
Cancel
2008-jan-17 10:47:54 net.sourceforge.clearcase.ClearCaseCLIImpl launch
FIN: cleartool exit value: 0
80
2008-jan-17 10:47:54 net.sourceforge.clearcase.ClearCaseCLIImpl parserOutputMKDIR
FINAST: parsing MKDIR: Created directory element "testdir1".
100
Operation done!

Unix/Linux
-----------

ws4359{mike_clearcase}> java -jar clearcase-java.jar
Number of args :0
Usage: java -jar clearcase-java.jar command [option]
Command - checkout,checkin,delete,create_dir, create_file,configspec,vobnames,viewnames,list
Option - *, element, list of elements, none

Example 1

ws4359{mike_clearcase}> java -jar clearcase-java.jar create_dir  testdir1
Number of args :2
Command : create_dir
Options :
testdir1
Started ...
Jan 17, 2008 11:52:41 AM net.sourceforge.clearcase.ClearCaseCLIImpl launch
FINE: launching: cleartool mkdir -nc testdir1
Cancel
Jan 17, 2008 11:52:41 AM net.sourceforge.clearcase.ClearCaseCLIImpl launch
FINE: cleartool exit value: 0
80
Jan 17, 2008 11:52:41 AM net.sourceforge.clearcase.ClearCaseCLIImpl parserOutputMKDIR
FINEST: parsing MKDIR: Created directory element "testdir1".
100
Operation done!



  
Build from source
=================

If you want to build the framework you simply need to
use  the build.xml provided in the project directory.

There are a number of targets.

* dist -    builds a jar file and puts it under build.lib-dir.
* release - builds a zip file in directory called net.sourceforge.clearcase_x.x.x.zip
            where x denotes version numbers.
* clean   - removes all files from build and dist directory.            

Develop source
==============  

TBD.

