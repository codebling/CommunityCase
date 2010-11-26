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
package org.community.intellij.plugins.communitycase.update;

import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.update.UpdateSession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * update session implementation
 */
public class UpdateSession implements com.intellij.openapi.vcs.update.UpdateSession {
  private final List<VcsException> exceptions;

  public UpdateSession(@Nullable List<VcsException> exceptions) {
    if (exceptions == null) {
      this.exceptions = new ArrayList<VcsException>();
    }
    else {
      this.exceptions = exceptions;
    }
  }

  @NotNull
  public List<VcsException> getExceptions() {
    return exceptions;
  }

  public void onRefreshFilesCompleted() {
  }

  public boolean isCanceled() {
    return false;
  }
}
