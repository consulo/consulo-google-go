/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
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

package com.goide.dlv;

import java.net.InetSocketAddress;

import javax.annotation.Nonnull;

import org.jetbrains.debugger.Vm;
import org.jetbrains.debugger.connection.RemoteVmConnection;
import com.intellij.openapi.util.AsyncResult;
import consulo.builtInServer.impl.net.util.netty.NettyKt;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;

public class DlvRemoteVmConnection extends RemoteVmConnection {
  @Nonnull
  @Override
  public Bootstrap createBootstrap(@Nonnull InetSocketAddress address, @Nonnull AsyncResult<Vm> vmResult) {
    return NettyKt.oioClientBootstrap().handler(new ChannelInitializer() {
      @Override
      protected void initChannel(@Nonnull Channel channel) throws Exception {
        vmResult.setDone(new DlvVm(getDebugEventListener(), channel));
      }
    });
  }

  @Nonnull
  @Override
  protected String connectedAddressToPresentation(@Nonnull InetSocketAddress address, @Nonnull Vm vm) {
    return address.toString();
  }
}