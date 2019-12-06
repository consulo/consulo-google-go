package org.jetbrains.debugger;

import consulo.util.dataholder.UserDataHolderEx;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public interface Vm extends UserDataHolderEx
{
  DebugEventListener getDebugListener();

  default SuspendContextManager<? extends CallFrame> getSuspendContextManager() {
    return DummySuspendContextManager.INSTANCE;
  }
}
