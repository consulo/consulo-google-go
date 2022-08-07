package org.jetbrains.debugger;

import consulo.application.AllIcons;
import consulo.execution.debug.frame.XExecutionStack;
import consulo.execution.debug.frame.XStackFrame;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09-May-17
 */
@Deprecated
public class ExecutionStackView extends XExecutionStack {
  private SuspendContext mySuspendContext;

  public ExecutionStackView(SuspendContext suspendContext) {
    super("", AllIcons.Debugger.ThreadAtBreakpoint);
    mySuspendContext = suspendContext;
  }

  public SuspendContext getSuspendContext() {
    return mySuspendContext;
  }

  @Nullable
  @Override
  public XStackFrame getTopFrame() {
    return null;
  }

  @Override
  public void computeStackFrames(XStackFrameContainer container) {

  }
}
