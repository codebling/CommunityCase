cd c:\java\abbot-0.7.3
SET CLASS_PATH=%CLASS_PATH%;c:\java\intellij\lib\xerces.jar;c:\java\intellij\lib\idea.jar;c:\java\intellij\lib\icons.jar;c:\java\intellij\lib\jdom.jar;c:\java\intellij\lib\oromatcher.jar;c:\java\intellij\lib\jh.jar;c:\java\intellij\lib\ant.jar;c:\java\intellij\lib\optional.jar;c:\java\intellij\lib\junit.jar;c:\java\intellij\lib\servlet.jar;c:\java\intellij\lib\log4j.jar;c:\java\intellij\lib\velocity.jar;c:\java\intellij\lib\JNIWrap.jar;c:\java\intellij\lib\jasper-compiler.jar;c:\java\intellij\lib\jasper-runtime.jar;c:\java\intellij\resources\;C:\java\j2sdk1.4.1\lib\tools.jar;c:\java\intellij\plugins\lib\HiLightTool.jar
SET CLASS_PATH=c:\java\abbot-0.7.3\lib\abbot.jar;%CLASS_PATH%

SET JVMARGS=%JVMARGS% -Didea.system.path="c:\java\intellij\system"
SET JVMARGS=%JVMARGS% -Djdk.home="c:\java\j2sdk1.4.1"
SET JVMARGS=%JVMARGS% -Didea.config.path="c:\java\intellij\config"
SET JAVA_HOME=c:\java\j2sdk1.4.1

SET PATH=%JAVA_HOME%\bin;%PATH%
java %JVMARGS% -cp %CLASS_PATH% abbot.editor.Costello
