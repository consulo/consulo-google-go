package org.jetbrains.debugger;

import io.netty.util.concurrent.Promise;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface SuspendContext<CALL_FRAME extends CallFrame> {
  SuspendState getState();

  CALL_FRAME getTopFrame();

  Promise<CallFrame[]> getFrames();

  default Vm getVm() {
    throw new UnsupportedOperationException();
  }

  default ExceptionData getExceptionData() {
    return null;
  }
}
