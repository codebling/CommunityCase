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
package org.community.intellij.plugins.communitycase.changes;

import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * change list
 */
public class ChangeList extends LocalChangeList {
  private String name;
  private String comment;
  private final Collection<Change> changes;

  public ChangeList(@NotNull String name, String comment, Collection<Change> changes) {
    super();
    setName(name);
    setComment(comment);
    this.changes = changes;
  }

  /** {@inheritDoc} */
  @Override
  @NotNull
  public String getName() {
    return name;
  }

  /** {@inheritDoc} */
  @Override
  public void setName(@NotNull String name) {
    this.name = name;
  }

  /** {@inheritDoc} */
  @Override
  public String getComment() {
    return comment;
  }

  /** {@inheritDoc} */
  @Override
  public void setComment(String comment) {
    this.comment = comment;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isDefault() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isReadOnly() {
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public void setReadOnly(boolean isReadOnly) {
  }

  /** {@inheritDoc} */
  @Override
  public Collection<Change> getChanges() {
    return changes;
  }

  /** {@inheritDoc} */
  @Override
  public LocalChangeList copy() {
    return new org.community.intellij.plugins.communitycase.changes.ChangeList(name, comment, changes);
  }
}
