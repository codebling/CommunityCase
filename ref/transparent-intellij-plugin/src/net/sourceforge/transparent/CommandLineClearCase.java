package net.sourceforge.transparent;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;

// TODO: Turn the old implementation in ClearcaseCLI with the option to use a long lived clearcase or instanciate one for each command
public class CommandLineClearCase implements ClearCase {
   private static final Logger LOG = Logger.getInstance("net.sourceforge.transparent.CommandLineClearCase");

   public String getName() {
      return CommandLineClearCase.class.getName();
   }

   public void undoCheckOut(File file) {
      cleartool(new String[]{"unco","-rm", file.getAbsolutePath()});
   }

   public void checkIn(File file, String comment) {
      cleartool(new String[]{"ci","-c",quote(comment),"-identical",file.getAbsolutePath()});
   }

   public void checkOut(File file, boolean isReserved) {
      cleartool(new String[]{"co","-nc",isReserved ? "-reserved" : "-unreserved",file.getAbsolutePath()});

   }

   public void delete(File file, String comment) {
      cleartool(new String[]{"rmname","-force","-c",quote(comment),file.getAbsolutePath()});
   }

   public void add(File file, String comment) {
      if (file.isDirectory())
         doAddDir(file, comment);
      else
         doAdd("mkelem", file.getAbsolutePath(), comment);
   }

   private void doAddDir(File dir, String comment) {
      File tmpDir = new File(dir.getParentFile(), dir.getName()+".add");
      if (!dir.renameTo(tmpDir)) {
         throw new ClearCaseException("Could not rename " + dir.getPath() + " to " + tmpDir.getName());
      }
      try {
         doAdd("mkdir", dir.getAbsolutePath(), comment);
         if (!dir.delete()) {
            throw new ClearCaseException("Could not delete " + dir.getPath() + " as part of adding it to Clearcase");
         }
      } finally {
         if (!tmpDir.renameTo(dir)) {
            throw new ClearCaseException("Could not move back the content of " + dir.getPath() + " as part of adding it to Clearcase:\nIts old content is in " + tmpDir.getName() + ". Please move it back manually");
         }
      }
   }

   private void doAdd(String subcmd, String path, String comment) {
      cleartool(new String[]{subcmd,"-c",quote(comment),path});
   }

   public void move(File file, File target, String comment) {
      cleartool(new String[]{"mv","-c",quote(comment),file.getAbsolutePath(),target.getAbsolutePath()});
   }

   public boolean isElement(File file) {
      return getStatus(file) != Status.NOT_AN_ELEMENT;
   }

   /**
    * return true iff the file is a clearcase file
    */
   public boolean isCheckedOut(File file) {
      return getStatus(file) == Status.CHECKED_OUT;
   }

// cleartool ls -directory Release10.xml =>

//   NOT_AN_ELEMENT: cleartool: Error: Pathname is not within a VOB: "Release10.xml
//   NOT_AN_ELEMENT: cc-config.xml.mkel
//   CHECKED_IN:     Release10.xml@@\main\165                                 Rule: \main\LATEST
//   HIJACKED:       Release10.xml@@\main\165 [hijacked]                      Rule: \main\LATEST
//   CHECKED_OUT:    Release10.xml@@\main\CHECKEDOUT from \main\165           Rule: CHECKEDOUT
   public Status getStatus(File file) {
      Runner runner = cleartool(new String[]{"ls","-directory",file.getAbsolutePath()},true);
      if (!runner.isSuccessfull()) return Status.NOT_AN_ELEMENT;
      if (runner.getOutput().indexOf("@@") == -1) return Status.NOT_AN_ELEMENT;
      if (runner.getOutput().indexOf("[hijacked]") != -1) return Status.HIJACKED;
      if (runner.getOutput().indexOf("Rule: CHECKEDOUT") != -1) return Status.CHECKED_OUT;
      return Status.CHECKED_IN;
   }

   public void cleartool(String subcmd) {
      String cmd = "cleartool " + subcmd;
      LOG.debug(cmd);
      Runner runner = new Runner();
      runner.run(cmd);
   }

   public void cleartool(String[] subcmd) {
      cleartool(subcmd, false);
   }

   private Runner cleartool(String[] subcmd, boolean canFail) {
      String[] cmd = Runner.getCommand("cleartool", subcmd);
      LOG.debug(Runner.getCommandLine(cmd));
      Runner runner = new Runner();
      runner.run(cmd, canFail);
      return runner;
   }

   public CheckedOutStatus getCheckedOutStatus(File file) {
      Runner runner = cleartool(
            new String[]{"lscheckout", "-fmt", "%Rf", "-directory", file.getAbsolutePath()},true);
      if (!runner.isSuccessfull()) return CheckedOutStatus.NOT_CHECKED_OUT;
      if (runner.getOutput().equalsIgnoreCase("reserved")) return CheckedOutStatus.RESERVED;
      if (runner.getOutput().equalsIgnoreCase("unreserved")) return CheckedOutStatus.UNRESERVED;
      if (runner.getOutput().equals("")) return CheckedOutStatus.NOT_CHECKED_OUT;
      return CheckedOutStatus.NOT_CHECKED_OUT;
   }

   public static String quote(String str)
	{
		return "\"" + str.replaceAll("\"","\\\"") + "\"";
	}


}
