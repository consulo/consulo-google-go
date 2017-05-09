package org.jetbrains.debugger;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public abstract class SuspendContextBase<F extends CallFrame> implements SuspendContext<F> {
  private boolean explicitPaused;

  public SuspendContextBase(boolean explicitPaused) {
    this.explicitPaused = explicitPaused;
  }

  @Override
  public SuspendState getState() {
    ExceptionData exceptionData = getExceptionData();
    if (exceptionData == null) {
      return explicitPaused ? SuspendState.PAUSED : SuspendState.NORMAL;
    }
    return SuspendState.EXCEPTION;
  }
}
