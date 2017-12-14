package org.jetbrains.debugger;

import com.intellij.openapi.util.Comparing;
import io.netty.util.concurrent.Promise;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public interface SuspendContextManager<CALL_FRAME extends CallFrame> {
  /**
   * Tries to suspend VM. If successful, [DebugEventListener.suspended] will be called.
   */
  Promise<?> suspend();

  SuspendContext<CALL_FRAME> getContext();

  SuspendContext<CALL_FRAME> getContextOrFault();

  default boolean isContextObsolete(SuspendContext<?> context) {
    return !Comparing.equal(context, getContext());
  }

  void setOverlayMessage(String message);


  default Promise<?> continueVm(StepAction stepAction) {
    return continueVm(stepAction, 1);
  }

  /**
   * Resumes the VM execution. This context becomes invalid until another context is supplied through the
   * [DebugEventListener.suspended] event.
   *
   * @param stepAction to perform
   *                   *
   * @param stepCount  steps to perform (not used if `stepAction == CONTINUE`)
   */
  Promise<?> continueVm(StepAction stepAction, int stepCount);

  boolean isRestartFrameSupported();

  /**
   * Restarts a frame (all frames above are dropped from the stack, this frame is started over).
   * for success the boolean parameter
   * is true if VM has been resumed and is expected to get suspended again in a moment (with
   * a standard 'resumed' notification), and is false if call frames list is already updated
   * without VM state change (this case presently is never actually happening)
   */
  Promise<Boolean> restartFrame(CALL_FRAME callFrame);

  /**
   * @return whether reset operation is supported for the particular callFrame
   */
  boolean canRestartFrame(CallFrame callFrame);
}
