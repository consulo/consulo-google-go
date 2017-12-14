package org.jetbrains.debugger;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.AtomicNotNullLazyValue;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.breakpoints.XBreakpointHandler;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.intellij.xdebugger.stepping.XSmartStepIntoHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.connection.VmConnection;

import javax.swing.event.HyperlinkListener;

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

  private AtomicNotNullLazyValue<XBreakpointHandler<?>[]> myBreakpointHandlerValue = new AtomicNotNullLazyValue<XBreakpointHandler<?>[]>() {
    @NotNull
    @Override
    protected XBreakpointHandler<?>[] compute() {
      return createBreakpointHandlers();
    }
  };

  protected DebugProcessImpl(@NotNull XDebugSession session,
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
    if (context instanceof SuspendContextView) {
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

  @NotNull
  @Override
  public XBreakpointHandler<?>[] getBreakpointHandlers() {
    switch (myConnection.getState().getStatus()) {
      case DISCONNECTED:
      case CONNECTION_FAILED:
        return XBreakpointHandler.EMPTY_ARRAY;
      default:
        return myBreakpointHandlerValue.getValue();
    }
  }

  // some VM (firefox for example) doesn't implement step out correctly, so, we need to fix it
  protected boolean isVmStepOutCorrect() {
    return true;
  }

  @NotNull
  protected abstract XBreakpointHandler<?>[] createBreakpointHandlers();

  public C getConnection() {
    return myConnection;
  }

  public ExecutionResult getExecutionResult() {
    return myExecutionResult;
  }

  @NotNull
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
    return myConnection.getState().getMessageLinkListener();
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
