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

import com.goide.GoConstants;
import com.goide.GoFileType;
import com.goide.dlv.breakpoint.DlvBreakpointProperties;
import com.goide.dlv.breakpoint.DlvBreakpointType;
import com.goide.dlv.protocol.DlvRequest;
import com.goide.util.GoUtil;
import consulo.application.AccessToken;
import consulo.application.ReadAction;
import consulo.disposer.Disposable;
import consulo.execution.ExecutionResult;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XSourcePosition;
import consulo.execution.debug.breakpoint.XBreakpoint;
import consulo.execution.debug.breakpoint.XBreakpointHandler;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.evaluation.XDebuggerEditorsProviderBase;
import consulo.execution.debug.frame.XSuspendContext;
import consulo.execution.debug.icon.ExecutionDebugIconGroup;
import consulo.execution.ui.ExecutionConsole;
import consulo.google.go.run.dlv.DlvSuspendContext;
import consulo.google.go.run.dlv.api.DlvRequests;
import consulo.google.go.run.dlv.api.SimpleInOutMessage;
import consulo.language.plain.PlainTextLanguage;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.concurrent.AsyncResult;
import consulo.util.socketConnection.ConnectionStatus;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.debugger.DebugProcessImpl;
import org.jetbrains.debugger.StepAction;
import org.jetbrains.debugger.Vm;
import org.jetbrains.debugger.connection.VmConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.goide.dlv.protocol.DlvApi.*;
import static consulo.util.lang.ObjectUtil.assertNotNull;
import static consulo.util.lang.ObjectUtil.tryCast;

public final class DlvDebugProcess extends DebugProcessImpl<VmConnection<?>> implements Disposable {
  public static final boolean IS_DLV_DISABLED = !GoConstants.AMD64.equals(GoUtil.systemArch());

  private final static Logger LOG = Logger.getInstance(DlvDebugProcess.class);
  private final AtomicBoolean breakpointsInitiated = new AtomicBoolean();
  private final AtomicBoolean connectedListenerAdded = new AtomicBoolean();
  private static final java.util.function.Consumer<Throwable> THROWABLE_CONSUMER = LOG::warn;

  @Nonnull
  private final java.util.function.Consumer<CommandOut> myStateConsumer = new java.util.function.Consumer<CommandOut>() {
    @Override
    public void accept(@Nonnull final CommandOut so) {
      DebuggerState o = so.State;
      if (o.exited) {
        stop();
        return;
      }

      final XBreakpoint<DlvBreakpointProperties> find = findBreak(o.currentThread.breakPoint);
      DlvSuspendContext context = new DlvSuspendContext(DlvDebugProcess.this, o.currentThread, o.threads, getProcessor());
      XDebugSession session = getSession();
      if (find == null) {
        session.positionReached(context);
      }
      else {
        session.breakpointReached(find, null, context);
      }
    }

    @Nullable
    private XBreakpoint<DlvBreakpointProperties> findBreak(@Nullable Breakpoint point) {
      if (point == null) {
        return null;
      }

      for (Map.Entry<XBreakpoint<DlvBreakpointProperties>, Integer> entry : myBreakpoints.entrySet()) {
        if (entry.getValue().equals(point.id)) {
          return entry.getKey();
        }
      }
      return null;
    }
  };

  @Nonnull
  public <T> AsyncResult<T> send(@Nonnull SimpleInOutMessage<?, T> request) {
    return send(request, getProcessor());
  }

  @Nonnull
  public static <T> AsyncResult<T> send(@Nonnull SimpleInOutMessage<?, T> request, @Nonnull DlvCommandProcessor processor) {
    AsyncResult<T> send = processor.send(request);
    send.doWhenRejectedWithThrowable(THROWABLE_CONSUMER);
    return send;
  }

  @Nonnull
  public <T> AsyncResult<T> send(@Nonnull DlvRequest<T> request) {
    return send(request, getProcessor());
  }

  @Nonnull
  public static <T> AsyncResult<T> send(@Nonnull DlvRequest<T> request, @Nonnull DlvCommandProcessor processor) {
    AsyncResult<T> send = processor.send(request);
    send.doWhenRejectedWithThrowable(THROWABLE_CONSUMER);
    return send;
  }

  @Nonnull
  private DlvCommandProcessor getProcessor() {
    return assertNotNull(tryCast(getVm(), DlvVm.class)).getCommandProcessor();
  }

  public DlvDebugProcess(@Nonnull XDebugSession session, @Nonnull VmConnection<?> connection, @Nullable ExecutionResult er) {
    super(session, connection, new MyEditorsProvider(), null, er);
  }

  @Nonnull
  @Override
  protected XBreakpointHandler<?>[] createBreakpointHandlers() {
    return new XBreakpointHandler[]{new MyBreakpointHandler()};
  }

  @Nonnull
  @Override
  public ExecutionConsole createConsole() {
    ExecutionResult executionResult = getExecutionResult();
    return executionResult == null ? super.createConsole() : executionResult.getExecutionConsole();
  }

  @Override
  public void dispose() {
    // todo
  }

  @Override
  public boolean checkCanInitBreakpoints() {
    if (getConnection().getState().getStatus() == ConnectionStatus.CONNECTED) {
      // breakpointsInitiated could be set in another thread and at this point work (init breakpoints) could be not yet performed
      return initBreakpointHandlersAndSetBreakpoints(false);
    }

    if (connectedListenerAdded.compareAndSet(false, true)) {
      getConnection().addListener(status -> {
        if (status == ConnectionStatus.CONNECTED) {
          initBreakpointHandlersAndSetBreakpoints(true);
        }
      });
    }
    return false;
  }

  private boolean initBreakpointHandlersAndSetBreakpoints(boolean setBreakpoints) {
    if (!breakpointsInitiated.compareAndSet(false, true)) return false;

    Vm vm = getVm();
    assert vm != null : "Vm should be initialized";

    if (setBreakpoints) {
      doSetBreakpoints();
      resume(vm);
    }

    return true;
  }

  private void doSetBreakpoints() {
    AccessToken token = ReadAction.start();
    try {
      getSession().initBreakpoints();
    }
    finally {
      token.finish();
    }
  }

  private void command(@Nonnull @MagicConstant(stringValues = {NEXT, CONTINUE, HALT, SWITCH_THREAD, STEP, STEPOUT}) String name) {
    send(new DlvRequest.Command(name)).doWhenDone(myStateConsumer).doWhenRejectedWithThrowable(LOG::warn);
  }

  @Nullable
  @Override
  protected AsyncResult<?> continueVm(@Nonnull Vm vm, @Nonnull StepAction stepAction) {
    switch (stepAction) {
      case CONTINUE:
        command(CONTINUE);
        break;
      case IN:
        command(STEP);
        break;
      case OVER:
        command(NEXT);
        break;
      case OUT:
        command(STEPOUT);
        break;
    }
    return null;
  }

  /*@NotNull
  @Override
  public List<Location> getLocationsForBreakpoint(@NotNull XLineBreakpoint<?> breakpoint) {
    return Collections.emptyList();
  }*/

  @Override
  public void runToPosition(@Nonnull XSourcePosition position, @Nullable XSuspendContext context) {
    // todo
  }

  @Override
  public void stop() {
    if (getVm() != null) {
      send(new DlvRequest.Detach(true));
    }
    getSession().stop();
  }

  private static class MyEditorsProvider extends XDebuggerEditorsProviderBase {
    @Nonnull
    @Override
    public FileType getFileType() {
      return GoFileType.INSTANCE;
    }

    @Override
    protected PsiFile createExpressionCodeFragment(@Nonnull Project project, @Nonnull String text, @Nullable PsiElement context, boolean isPhysical) {
      return PsiFileFactory.getInstance(project).createFileFromText("dlv-debug.txt", PlainTextLanguage.INSTANCE, text);
    }
  }

  private final Map<XBreakpoint<DlvBreakpointProperties>, Integer> myBreakpoints = ContainerUtil.createConcurrentWeakMap();

  private class MyBreakpointHandler extends XBreakpointHandler<XLineBreakpoint<DlvBreakpointProperties>> {

    public MyBreakpointHandler() {
      super(DlvBreakpointType.class);
    }

    @Override
    public void registerBreakpoint(@Nonnull XLineBreakpoint<DlvBreakpointProperties> breakpoint) {
      XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
      if (breakpointPosition == null) return;
      VirtualFile file = breakpointPosition.getFile();
      int line = breakpointPosition.getLine();

      send(DlvRequests.CreateBreakpoint.build(new Breakpoint(line + 1, file.getPath()))).doWhenDone(b -> {
        myBreakpoints.put(breakpoint, b.Breakpoint.id);
        getSession().updateBreakpointPresentation(breakpoint, ExecutionDebugIconGroup.breakpointBreakpointvalid(), null);
      }).doWhenRejectedWithThrowable(t -> {
        String message = t == null ? null : t.getMessage();
        getSession().updateBreakpointPresentation(breakpoint, ExecutionDebugIconGroup.breakpointBreakpointinvalid(), message);
      });

    }

    @Override
    public void unregisterBreakpoint(@Nonnull XLineBreakpoint<DlvBreakpointProperties> breakpoint, boolean temporary) {
      XSourcePosition breakpointPosition = breakpoint.getSourcePosition();
      if (breakpointPosition == null) return;
      Integer vmBreakpointId = myBreakpoints.remove(breakpoint);
      if (vmBreakpointId == null) return;
      send(new DlvRequest.ClearBreakpoint(vmBreakpointId));
    }
  }
}