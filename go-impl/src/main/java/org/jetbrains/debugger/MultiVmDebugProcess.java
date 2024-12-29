package org.jetbrains.debugger;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 09-May-17
 */
public interface MultiVmDebugProcess {
  @Nullable
  Vm getMainVm();

  @Nullable
  Vm getActiveOrMainVm();
}
