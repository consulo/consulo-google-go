package org.jetbrains.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.Nullable;

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
