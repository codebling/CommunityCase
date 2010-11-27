/*
 * Copyright 2000-2010 JetBrains s.r.o.
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

import com.intellij.ide.XmlRpcServer;
import com.intellij.openapi.components.ServiceManager;
import org.jetbrains.annotations.NotNull;

/**
 * The git ssh service implementation that uses IDEA XML RCP service
 */
public class SshIdeaService { //extends SshService {
  /**
   * XML RPC server
   */
  private final XmlRpcServer myXmlRpcServer;

  /**
   * A constructor from parameter
   *
   * @param xmlRpcServer the injected XmlRpc server reference
   */
  public SshIdeaService(final @NotNull XmlRpcServer xmlRpcServer) {
    myXmlRpcServer = xmlRpcServer;
  }

  /**
   * @return an instance of the server
   */
  @NotNull
  public static SshIdeaService getInstance() {
    final SshIdeaService service = ServiceManager.getService(SshIdeaService.class);
    if (service == null) {
      throw new IllegalStateException("The service " + SshIdeaService.class.getName() + " cannot be located");
    }
    return service;
  }

  public int getXmlRcpPort() {
    return myXmlRpcServer.getPortNumber();
  }
/*
  protected void registerInternalHandler(final String handlerName, final SshHandler handler) {
    myXmlRpcServer.addHandler(handlerName, handler);
  }*/
}
