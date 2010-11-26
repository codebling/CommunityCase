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
package org.community.intellij.plugins.communitycase;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import org.community.intellij.plugins.communitycase.commands.Command;
import org.community.intellij.plugins.communitycase.commands.SimpleHandler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The tag reference object
 */
public class Tag extends Reference {
  /**
   * Prefix for tags ({@value})
   */
  @NonNls public static final String REFS_TAGS_PREFIX = "refs/tags/";

  /**
   * The constructor
   *
   * @param name the used name
   */
  public Tag(@NotNull String name) {
    super(name);
  }

  /**
   * {@inheritDoc}
   */
  @NotNull
  public String getFullName() {
    return REFS_TAGS_PREFIX + myName;
  }

  /**
   * List tags for the  root
   *
   * @param project the context
   * @param root    the  root
   * @param tags    the tag list
   * @param containingCommit
   * @throws com.intellij.openapi.vcs.VcsException if there is a problem with running
   */
  public static void listAsStrings(final Project project, final VirtualFile root, final Collection<String> tags,
                                   @Nullable final String containingCommit) throws VcsException {
    SimpleHandler handler = new SimpleHandler(project, root, Command.TAG);
    handler.setNoSSH(true);
    handler.setSilent(true);
    handler.addParameters("-l");
    if (containingCommit != null) {
      handler.addParameters("--contains");
      handler.addParameters(containingCommit);
    }
    for (String line : handler.run().split("\n")) {
      if (line.length() == 0) {
        continue;
      }
      tags.add(new String(line));
    }
  }

  /**
   * List tags for the  root
   *
   * @param project the context
   * @param root    the  root
   * @param tags    the tag list
   * @throws com.intellij.openapi.vcs.VcsException if there is a problem with running
   */
  public static void list(final Project project, final VirtualFile root, final Collection<? super Tag> tags) throws VcsException {
    ArrayList<String> temp = new ArrayList<String>();
    listAsStrings(project, root, temp, null);
    for (String t : temp) {
      tags.add(new Tag(t));
    }
  }
}
