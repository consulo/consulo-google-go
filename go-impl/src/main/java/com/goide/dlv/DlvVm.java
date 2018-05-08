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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.io.NettyKt;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.json.JsonObjectDecoder;
import javax.annotation.Nonnull;
import org.jetbrains.debugger.AttachStateManager;
import org.jetbrains.debugger.DebugEventListener;
import org.jetbrains.debugger.StandaloneVmHelper;
import org.jetbrains.debugger.VmBase;
import org.jetbrains.io.SimpleChannelInboundHandlerAdapter;
import org.jetbrains.jsonProtocol.Request;

import java.io.IOException;

public class DlvVm extends VmBase {
  private final static Logger LOG = Logger.getInstance(DlvVm.class);

  @Nonnull
  private final DlvCommandProcessor commandProcessor;
  @Nonnull
  private final StandaloneVmHelper vmHelper;
  // @NotNull private final DummyBreakpointManager breakpointManager = new DummyBreakpointManager();

  public DlvVm(@Nonnull DebugEventListener tabListener, @Nonnull Channel channel) {
    super(tabListener);

    commandProcessor = new DlvCommandProcessor() {
      @Override
      public boolean write(@Nonnull Request message) throws IOException {
        ByteBuf content = message.getBuffer();
        LOG.info("OUT: " + content.toString(CharsetToolkit.UTF8_CHARSET));
        return vmHelper.write(content);
      }
    };
    vmHelper = new StandaloneVmHelper(this, commandProcessor, channel);

    channel.pipeline().addLast(new JsonObjectDecoder(), new SimpleChannelInboundHandlerAdapter() {
      @Override
      protected void messageReceived(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof ByteBuf) {
          LOG.info("IN: " + ((ByteBuf)message).toString(CharsetToolkit.UTF8_CHARSET));
          String json = NettyKt.readUtf8((ByteBuf)message);
          JsonParser parser = new JsonParser();
          JsonElement jsonElement = parser.parse(json);
          getCommandProcessor().processIncomingJson(jsonElement);
        }
      }
    });
  }

  @Nonnull
  @Override
  public AttachStateManager getAttachStateManager() {
    return vmHelper;
  }

  @Nonnull
  public final DlvCommandProcessor getCommandProcessor() {
    return commandProcessor;
  }

 /* @NotNull
  @Override
  public ScriptManagerBase<ScriptBase> getScriptManager() {
    throw new UnsupportedOperationException();
  }

  @NotNull
  @Override
  public BreakpointManager getBreakpointManager() {
    return breakpointManager;
  }   */

  /**
   * Changed API between minor versions, runtime is compatible
   * Todo: uncomment since 2016.2, when only the last build of 15.0 will be supported
   */
  /*@SuppressWarnings("unchecked")
  @NotNull
  @Override
  public SuspendContextManagerBase getSuspendContextManager() {
    return new SuspendContextManagerBase() {
      @NotNull
      @Override
      public Promise<Void> continueVm(@NotNull StepAction stepAction, int stepCount) {
        return Promise.DONE;
      }

      @NotNull
      @Override
      protected DebugEventListener getDebugListener() {
        return DlvVm.this.getDebugListener();
      }

      @NotNull
      @Override
      protected Promise<?> doSuspend() {
        return Promise.DONE;
      }
    };
  } */
}