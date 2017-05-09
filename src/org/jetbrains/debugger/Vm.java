package org.jetbrains.debugger;

import com.intellij.openapi.util.UserDataHolderEx;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface Vm extends UserDataHolderEx {
  DebugEventListener getDebugListener();

  default SuspendContextManager<? extends CallFrame> getSuspendContextManager() {
    return DummySuspendContextManager.INSTANCE;
  }
}
