/*
 *  Copyright (c) 2002 Sabre, Inc. All rights reserved. 
 */
package net.sourceforge.transparent.actions;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vcs.VcsException;
import net.sourceforge.transparent.ClearCaseFile;
import net.sourceforge.transparent.ClearCaseException;

import java.util.List;
import java.util.ArrayList;

class ActionThread extends Thread {
   private final VirtualFile virtualFile;
   private final ActionContext context;
   private final ClearCaseFile file;
   private AsynchronousAction asynchronousAction;

   public ActionThread(AsynchronousAction asynchronousAction, ClearCaseFile file, VirtualFile vFile, ActionContext context) {
      super(asynchronousAction.getActionName(context));
      this.asynchronousAction = asynchronousAction;
      this.virtualFile = vFile;
      this.context = context;
      this.file = file;
   }

}
