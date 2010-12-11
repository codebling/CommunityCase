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

/**
 * The descriptor of git command. It contains policy information about myLocking and GUI thread policy.
 */
public class Command {

  public static final Command ADD = write("mkelem");
  public static final Command ANNOTATE = read("annotate");            //fixme
  public static final Command BRANCH = meta("branch");                //fixme
  public static final Command CHECKOUT = write("checkout");           //fixme
  public static final Command COMMIT = write("commit");               //fixme
  public static final Command CONFIG = meta("config");                //fixme
  public static final Command CHECK_ATTR = read("check-attr");        //fixme
  public static final Command CHERRY_PICK = write("cherry-pick");     //fixme
  public static final Command CLONE = write("clone");                 //fixme
  public static final Command DESCRIBE = meta("describe");            //fixme
  public static final Command DIFF = read("diff");                    //fixme
  public static final Command DIFF_INDEX = read("diff-index");        //fixme
  public static final Command FETCH = write("fetch");                 //fixme
  public static final Command INIT = write("init");                   //fixme
  public static final Command LOG = meta("lsh");
  public static final Command LS_FILES = read("ls-files");            //fixme
  public static final Command LS_REMOTE = meta("ls-remote");          //fixme
  public static final Command MERGE = write("merge");                 //fixme
  public static final Command MERGE_BASE = meta("merge-base");        //fixme
  public static final Command PULL = write("pull");                   //fixme
  public static final Command PUSH = write("push");                   //fixme
  public static final Command REBASE = writeSuspendable("rebase");    //fixme
  public static final Command REMOTE = meta("remote");                //fixme
  public static final Command RESET = write("reset");                 //fixme
  public static final Command REV_LIST = meta("rev-list");            //fixme
  public static final Command RM = write("rmelem");
  public static final Command SHOW = write("show");                   //fixme
  public static final Command STASH = write("stash");                 //fixme
  public static final Command TAG = meta("tag");                      //fixme
  public static final Command UPDATE_INDEX = write("update-index");   //fixme
  public static final Command VERSION = meta("-ver");

  // these commands modify .git/index
  private static final Command[] INDEX_MODIFIERS = {ADD, BRANCH, CHECKOUT, COMMIT, MERGE, RESET, RM, STASH};
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
    META,
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
