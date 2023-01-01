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

import com.goide.dlv.DlvCommandProcessor;
import com.goide.dlv.DlvDebugProcess;
import com.goide.dlv.DlvStackFrame;
import com.goide.dlv.protocol.DlvApi;
import consulo.execution.debug.frame.XExecutionStack;
import consulo.execution.debug.frame.XStackFrame;
import consulo.google.go.run.dlv.api.DlvRequests;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DlvExecutionStack extends XExecutionStack {
  @Nonnull
  private final DlvDebugProcess myProcess;
  private final int myThreadId;
  private final int myGoroutineId;
  private DlvCommandProcessor myProcessor;

  public DlvExecutionStack(@Nonnull DlvDebugProcess process, int threadId, int goroutineId, DlvCommandProcessor processor) {
    super("Goroutine # " + goroutineId + " Thread #" + threadId);
    myProcess = process;
    myThreadId = threadId;
    myGoroutineId = goroutineId;
    myProcessor = processor;
  }

  @Nullable
  @Override
  public XStackFrame getTopFrame() {
    return null;
  }

  @Override
  public void computeStackFrames(@Nonnull XStackFrameContainer container) {
    myProcess.send(DlvRequests.Stacktrace.build(myGoroutineId, 100)).doWhenDone(stacktraceOut -> {
      List<DlvStackFrame> list = new ArrayList<>(stacktraceOut.Locations.size());
      for (DlvApi.Location location : stacktraceOut.Locations) {
        DlvStackFrame frame = new DlvStackFrame(myProcess, location, myProcessor, list.size(), myGoroutineId);

        list.add(frame);
      }

      container.addStackFrames(list, true);
    });
  }
}
