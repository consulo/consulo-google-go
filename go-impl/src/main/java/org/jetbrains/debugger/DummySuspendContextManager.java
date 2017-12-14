package org.jetbrains.debugger;

import io.netty.util.concurrent.Promise;

/**
 * @author VISTALL
 * @since 09-May-17
 */
@Deprecated
public class DummySuspendContextManager implements SuspendContextManager<CallFrame> {
  public static final DummySuspendContextManager INSTANCE = new DummySuspendContextManager();
  @Override
  public Promise<?> suspend() {
    return null;
  }

  @Override
  public SuspendContext<CallFrame> getContext() {
    return null;
  }

  @Override
  public SuspendContext<CallFrame> getContextOrFault() {
    return null;
  }

  @Override
  public void setOverlayMessage(String message) {

  }

  @Override
  public Promise<?> continueVm(StepAction stepAction, int stepCount) {
    return null;
  }

  @Override
  public boolean isRestartFrameSupported() {
    return false;
  }

  @Override
  public Promise<Boolean> restartFrame(CallFrame callFrame) {
    return null;
  }

  @Override
  public boolean canRestartFrame(CallFrame callFrame) {
    return false;
  }
}
