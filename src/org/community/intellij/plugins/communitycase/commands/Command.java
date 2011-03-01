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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


//todo wc fix commands.. need separate classes for each command
//todo wc make all clearcase commands non-blocking (long clearcase commands block UI)
/**
 * The descriptor of git command. It contains policy information about myLocking and GUI thread policy.
 */
public class Command {

  public static final Command ADD = write("mkelem");
  public static final Command ANNOTATE = read("NOTIMPLEMENTED_ANNOTATE");            //fixme
  public static final Command BRANCH = meta("lstype -kind brtype -s");
  public static final Command CHECKIN = write("ci");
  public static final Command CHECKOUT= write("co");
  public static final Command GIT_CHECKOUT= write("NOTIMPLEMENTED_GIT_CHECKOUT");           //fixme
  public static final Command CONFIG = meta("NOTIMPLEMENTED_CONFIG");                //fixme
  public static final Command CHECK_ATTR = read("NOTIMPLEMENTED_CHECK_ATTR");        //fixme
  public static final Command CHERRY_PICK = write("NOTIMPLEMENTED_CHERRY_PICK");     //fixme
  public static final Command CLONE = write("NOTIMPLEMENTED_CLONE");                 //fixme
  public static final Command DESCRIBE = meta("NOTIMPLEMENTED_DESCRIBE");            //fixme
  public static final Command DIFF = read("NOTIMPLEMENTED_DIFF");                    //fixme
  public static final Command DIFF_INDEX = read("NOTIMPLEMENTED_DIFF_INDEX");        //fixme
  public static final Command FETCH = write("NOTIMPLEMENTED_FETCH");                 //fixme
  public static final Command INIT = write("NOTIMPLEMENTED_INIT");                   //fixme
  public static final Command LOG = meta("lsh");
  public static final Command LS = read("ls");
  public static final Command LS_CHECKOUTS = read("lsco");
  public static final Command LS_FILES = read("NOTIMPLEMENTED_LS_FILES");            //fixme
  public static final Command LS_REMOTE = meta("NOTIMPLEMENTED_LS_REMOTE");          //fixme
  public static final Command MERGE = write("NOTIMPLEMENTED_MERGE");                 //fixme
  public static final Command MERGE_BASE = meta("NOTIMPLEMENTED_MERGE_BASE");        //fixme
  public static final Command PULL = write("NOTIMPLEMENTED_PULL");                   //fixme
  public static final Command PUSH = write("NOTIMPLEMENTED_PUSH");                   //fixme
  public static final Command REBASE = writeSuspendable("NOTIMPLEMENTED_REBASE");    //fixme
  public static final Command REMOTE = meta("NOTIMPLEMENTED_REMOTE");                //fixme
  public static final Command RESET = write("NOTIMPLEMENTED_RESET");                 //fixme
  public static final Command REV_LIST = meta("NOTIMPLEMENTED_REV_LIST");            //fixme
  public static final Command RM = write("rmname");
  public static final Command SHOW = write("get");
  public static final Command STASH = write("NOTIMPLEMENTED_STASH");        //fixme
  public static final Command TAG = meta("NOTIMPLEMENTED_TAG");           //fixme
  public static final Command UNDO_CHECKOUT = write("unco");
  public static final Command UPDATE = write("update");
  public static final Command UPDATE_INDEX = write("NOTIMPLEMENTED_UPDATE_INDEX"); //fixme
  public static final Command VERSION = meta("-ver");
  public static final Command VERSION_TREE_GRAPHICAL = meta("lsvtree -g");

  // these commands modify .git/index
  private static final Command[] INDEX_MODIFIERS = {ADD, CHECKIN, BRANCH,GIT_CHECKOUT,CHECKIN, MERGE, RESET, RM, STASH};
  static {
    for (Command command : INDEX_MODIFIERS) {
      command.myModifiesIndex = true;
    }
  }

  /** Name of environment variable that specifies editor for the git */
  public static final String CC_EDITOR_ENV = "CC_EDITOR";

  @NotNull @NonNls private final String myName; // command name passed to git
  @NotNull private final LockingPolicy myLocking; // Locking policy for the command
  @NotNull private final ThreadPolicy myThreading; // Thread policy for the command
  private boolean myModifiesIndex; // true if the command modifies .git/index

  /**
   * The constructor
   *
   * @param name      the command myName
   * @param locking   the myLocking policy
   * @param threading the thread policy
   */
  private Command(@NonNls @NotNull String name, @NotNull LockingPolicy locking, @NotNull ThreadPolicy threading) {
    this.myLocking = locking;
    this.myName = name;
    this.myThreading = threading;
  }

  /**
   * Create command descriptor that performs metadata operations only
   *
   * @param name the command myName
   * @return the created command object
   */
  private static Command meta(String name) {
    return new Command(name, LockingPolicy.META, ThreadPolicy.ANY);
  }

  /**
   * Create command descriptor that performs reads from index
   *
   * @param name the command myName
   * @return the create command objects
   */
  private static Command read(String name) {
    return new Command(name, LockingPolicy.READ, ThreadPolicy.BACKGROUND_ONLY);
  }

  /**
   * Create command descriptor that performs write operations
   *
   * @param name the command myName
   * @return the created command object
   */
  private static Command write(String name) {
    return new Command(name, LockingPolicy.WRITE, ThreadPolicy.BACKGROUND_ONLY);
  }

  /**
   * Create command descriptor that performs write operations
   *
   * @param name the command myName
   * @return the created command object
   */
  private static Command writeSuspendable(String name) {
    return new Command(name, LockingPolicy.WRITE_SUSPENDABLE, ThreadPolicy.BACKGROUND_ONLY);
  }

  /**
   * @return the command name
   */
  @NotNull
  public String name() {
    return myName;
  }

  /**
   * @return the locking policy for the command
   */
  @NotNull
  public LockingPolicy lockingPolicy() {
    return myLocking;
  }

  /**
   * @return the locking policy for the command
   */
  @NotNull
  public ThreadPolicy threadingPolicy() {
    return myThreading;
  }

  /**
   * @return true if this command modifies .git/index file
   */
  public boolean modifiesIndex() {
    return myModifiesIndex;
  }

  /**
   * The myLocking policy for the command
   */
  enum LockingPolicy {
    /**
     * Read lock should be acquired for the command
     */
    READ,
    /**
     * Write lock should be acquired for the command
     */
    WRITE,
    /**
     * Write lock should be acquired for the command, and it could be acquired in several intervals
     */
    WRITE_SUSPENDABLE,
    /**
     * Metadata read/write command
     */
    META
  }

  /**
   * Thread policy for command
   */
  enum ThreadPolicy {
    /**
     * Any thread could be used
     */
    ANY,
    /**
     * Only background thread could be used
     */
    BACKGROUND_ONLY
  }
}
