package org.jetbrains.debugger;

import consulo.execution.ExecutionResult;
import consulo.execution.debug.DefaultDebugProcessHandler;
import consulo.execution.debug.XDebugProcess;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XBreakpointHandler;
import consulo.execution.debug.evaluation.XDebuggerEditorsProvider;
import consulo.execution.debug.frame.XExecutionStack;
import consulo.execution.debug.frame.XSuspendContext;
import consulo.execution.debug.step.XSmartStepIntoHandler;
import consulo.process.ProcessHandler;
import consulo.util.concurrent.AsyncResult;
import consulo.util.lang.lazy.LazyValue;
import org.jetbrains.debugger.connection.VmConnection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.event.HyperlinkListener;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public abstract class DebugProcessImpl<C extends VmConnection<?>> extends XDebugProcess implements MultiVmDebugProcess {
  private final C myConnection;
  private final XDebuggerEditorsProvider myEditorsProvider;
  private final XSmartStepIntoHandler<?> mySmartStepIntoHandler;
  private volatile CallFrame myLastCallFrame;
  private final ExecutionResult myExecutionResult;
  private boolean isForceStep;

  private Supplier<XBreakpointHandler<?>[]> myBreakpointHandlerValue = LazyValue.atomicNotNull(() -> createBreakpointHandlers());

  protected DebugProcessImpl(@Nonnull XDebugSession session,
                             C connection,
                             XDebuggerEditorsProvider editorsProvider,
                             XSmartStepIntoHandler<?> smartStepIntoHandler,
                             ExecutionResult executionResult) {
    super(session);
    myConnection = connection;
    myEditorsProvider = editorsProvider;
    mySmartStepIntoHandler = smartStepIntoHandler;
    myExecutionResult = executionResult;
  }

  public void resume(Vm vm) {
    continueVm(vm, StepAction.CONTINUE);
  }

  @Override
  @Nullable
  public Vm getMainVm() {
    return myConnection.getVm();
  }

  @Nullable
  @Override
  public Vm getActiveOrMainVm() {
    return getVm(getSession().getSuspendContext());
  }

  @Override
  public void resume(@Nullable XSuspendContext context) {
    continueVm(getVm(context), StepAction.CONTINUE);
  }

  @Override
  public void startStepInto(@Nullable XSuspendContext context) {
    Vm vm = getVm(context);
    updateLastCallFrame(vm);
    continueVm(vm, StepAction.IN);
  }

  @Override
  public void startForceStepInto(@Nullable XSuspendContext context) {
    startStepInto(context);
  }

  @Override
  public boolean checkCanPerformCommands() {
    return getActiveOrMainVm() != null;
  }

  @Override
  public void startStepOut(@Nullable XSuspendContext context) {
    Vm vm = getVm(context);
    if (isVmStepOutCorrect()) {
      myLastCallFrame = null;
    }
    else {
      updateLastCallFrame(vm);
    }
    continueVm(vm, StepAction.OUT);
  }

  @Override
  public void startStepOver(@Nullable XSuspendContext context) {
    Vm vm = getVm(context);
    updateLastCallFrame(vm);
    continueVm(vm, StepAction.OVER);
  }

  private void updateLastCallFrame(Vm vm) {
    SuspendContext<? extends CallFrame> context = vm.getSuspendContextManager().getContext();
    myLastCallFrame = context == null ? null : context.getTopFrame();
  }

  private Vm getVm(XSuspendContext context) {
    if (context instanceof XSuspendContext) {
      XExecutionStack activeExecutionStack = context.getActiveExecutionStack();
      if (activeExecutionStack instanceof ExecutionStackView) {
        return ((ExecutionStackView)activeExecutionStack).getSuspendContext().getVm();
      }
    }
    return getMainVm();
  }

  @Override
  public boolean isValuesCustomSorted() {
    return true;
  }

  protected AsyncResult<?> continueVm(Vm vm, StepAction stepAction) {
    return AsyncResult.done(null);
  }

  @Nonnull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    switch (myConnection.getState().getStatus()) {
      case DISCONNECTED:
      case CONNECTION_FAILED:
        return XBreakpointHandler.EMPTY_ARRAY;
      default:
        return myBreakpointHandlerValue.get();
    }
  }

  // some VM (firefox for example) doesn't implement step out correctly, so, we need to fix it
  protected boolean isVmStepOutCorrect() {
    return true;
  }

  @Nonnull
  protected abstract XBreakpointHandler<?>[] createBreakpointHandlers();

  public C getConnection() {
    return myConnection;
  }

  public ExecutionResult getExecutionResult() {
    return myExecutionResult;
  }

  @Nonnull
  @Override
  public XDebuggerEditorsProvider getEditorsProvider() {
    return myEditorsProvider;
  }

  @Override
  public String getCurrentStateMessage() {
    return myConnection.getState().getMessage();
  }

  @Nullable
  @Override
  public HyperlinkListener getCurrentStateHyperlinkListener() {
    return (HyperlinkListener) myConnection.getState().getMessageLinkListener();
  }

  @Nullable
  @Override
  protected ProcessHandler doGetProcessHandler() {
    if (myExecutionResult != null && myExecutionResult.getProcessHandler() != null) {
      return myExecutionResult.getProcessHandler();
    }
    return new DefaultDebugProcessHandler() {
      @Override
      public boolean isSilentlyDestroyOnClose() {
        return true;
      }
    };
  }

  @Override
  public boolean isLibraryFrameFilterSupported() {
    return true;
  }

  public Vm getVm() {
    return myConnection == null ? null : myConnection.getVm();
  }
}
