package net.sourceforge.transparent;

import java.io.*;

public class Runner {
   private static final boolean DEBUG =    false;
   private StringBuffer _buffer =       new StringBuffer();
   private boolean successfull;

   private class Consumer implements Runnable {
      private BufferedReader _reader;
      private boolean _finished =   false;

      public Consumer(InputStream inputStream) {
         _reader = new BufferedReader(new InputStreamReader(inputStream));
      }

      public void run() {
         try {
            String line;
            while ((line = _reader.readLine()) != null) {
               if (DEBUG) System.out.println("      " + line);
               if (_buffer.length() != 0) _buffer.append("\n"); // not theadsafe, but who cares
               _buffer.append(line);
            }
            _finished = true;
         } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
         }
      }
   }

   private Process startProcess(String command) throws IOException, InterruptedException {
      if (DEBUG) System.out.println(command);

      Process process = Runtime.getRuntime().exec(command);
      consumeProcessOutputs(process);

      return process;
   }

   private Process startProcess(String[] command) throws IOException, InterruptedException {
      if (DEBUG) System.out.println(getCommandLine(command));

      Process process = Runtime.getRuntime().exec(command);
      consumeProcessOutputs(process);

      return process;
   }

   public static String getCommandLine(String[] command) {
      StringBuffer buf = new StringBuffer();
      for (int i = 0; i < command.length; i++) {
         buf.append(command[i]);
         buf.append(" "       );
      }
      return buf.toString();
   }

   private void consumeProcessOutputs(Process process) throws InterruptedException {
      Consumer outputConsumer = new Consumer(process.getInputStream());
      Consumer errorConsumer =  new Consumer(process.getErrorStream());
      new Thread(errorConsumer).start();
      outputConsumer.run();
      while (!errorConsumer._finished) {
         Thread.sleep(100);
      }
   }

   private boolean endProcess(Process process) throws InterruptedException {
      return process.waitFor() == 0;
   }

   public void runAsynchronously(String command) throws IOException {
      Runtime.getRuntime().exec(command);
   }

   public void runAsynchronously(String[] command) throws IOException {
      Runtime.getRuntime().exec(command);
   }

   public void run(String command) {
      run(command, false);
   }

   public void run(String[] command) {
      run(command, false);
   }

   public boolean run(String command, boolean canFail) {
      try {
         Process process = startProcess(command);

         successfull = endProcess(process);
         if (successfull) {
            return true;
         } else {
            if (!canFail) throw new ClearCaseException("Error executing " + command + " : " + _buffer);
            return false;
         }
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   public boolean run(String[] command, boolean canFail) {
      try {
         Process process = startProcess(command);

         successfull = endProcess(process);
         if (successfull) {
            return true;
         } else {
            if (!canFail) throw new ClearCaseException("Error executing " + getCommandLine(command) + " : " + _buffer);
            return false;
         }
      } catch (RuntimeException e) {
         throw e;
      } catch (Exception e) {
         throw new RuntimeException(e.getMessage());
      }
   }

   public boolean runCanFail(String command) {
      return run(command, true);
   }

   public boolean runCanFail(String[] command) {
      return run(command, true);
   }

   public String getOutput() {
      return _buffer.toString();
   }

   public boolean isSuccessfull() {
      return successfull;
   }

   public static String[] getCommand(String exec, String[] args) {
      String[] cmd = new String[args.length + 1];
      cmd[0] = exec;
      System.arraycopy(args, 0, cmd, 1, args.length);
      return cmd;
   }
}

