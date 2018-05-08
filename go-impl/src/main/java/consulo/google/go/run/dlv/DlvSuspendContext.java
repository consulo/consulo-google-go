/*
 * Copyright 2013-2017 consulo.io
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

package consulo.google.go.run.dlv;

import javax.annotation.Nonnull;

import com.goide.dlv.DlvCommandProcessor;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.protocol.DlvApi;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;

import javax.annotation.Nullable;

public class DlvSuspendContext extends XSuspendContext {
  @Nonnull
  private final DlvExecutionStack[] myStacks;
  private DlvExecutionStack myCurrentStack;

  public DlvSuspendContext(@Nonnull DlvDebugProcess process,
                           @Nonnull DlvApi.Thread currentThread,
                           @Nonnull DlvApi.Thread[] allThreads,
                           @Nonnull DlvCommandProcessor processor) {
    myStacks = new DlvExecutionStack[allThreads.length];
    for (int i = 0; i < allThreads.length; i++) {
      DlvApi.Thread thread = allThreads[i];
      myStacks[i] = new DlvExecutionStack(process, thread.id, thread.goroutineID, processor);

      if (thread.id == currentThread.id) {
        myCurrentStack = myStacks[i];
      }
    }
  }

  @Nullable
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myCurrentStack;
  }

  @Nonnull
  @Override
  public XExecutionStack[] getExecutionStacks() {
    return myStacks;
  }
}