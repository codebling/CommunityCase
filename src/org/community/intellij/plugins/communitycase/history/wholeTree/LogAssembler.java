/*
 * Copyright 2000-2010 JetBrains s.r.o.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.community.intellij.plugins.communitycase.history.wholeTree;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.util.List;

/**
 * @author irengrig
 */
public class LogAssembler implements Log {
  private final Project myProject;
  private LogUI myLogUI;
  private MediatorImpl myMediator;
  private DetailsLoaderImpl myDetailsLoader;
  private DetailsCache myDetailsCache;
  private LoadController myLoadController;
  private BigTableTableModel myTableModel;

  //@CalledInAwt
  public LogAssembler(final Project project) {
    myProject = project;
    final ModalityState current = ModalityState.current();
    myMediator = new MediatorImpl(myProject, current);

    myLogUI = new LogUI(myProject, myMediator);
    myTableModel = myLogUI.getTableModel();

    myDetailsLoader = new DetailsLoaderImpl(myProject);
    myDetailsCache = new DetailsCache(myProject, myLogUI.getUIRefresh(), myDetailsLoader, current);
    myDetailsLoader.setDetailsCache(myDetailsCache);
    myLogUI.setDetailsCache(myDetailsCache);
    myLogUI.createMe();

    // modality state?
    myLoadController = new LoadController(myProject, current, myMediator, myDetailsCache);

    myMediator.setLoader(myLoadController);
    myMediator.setTableModel(myTableModel);
    myMediator.setUIRefresh(myLogUI.getRefreshObject());

    myTableModel.setCache(myDetailsCache);
    Disposer.register(this, myLogUI);
  }

  @Override
  public JComponent getVisualComponent() {
    return myLogUI.getPanel();
  }

  @Override
  public void rootsChanged(List<VirtualFile> roots) {
    myLogUI.rootsChanged(roots);
  }

  @Override
  public void dispose() {
  }
}
