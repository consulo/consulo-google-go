package org.jetbrains.debugger;

import com.intellij.openapi.util.UserDataHolderBase;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public class VmBase extends UserDataHolderBase implements Vm, AttachStateManager {
  private final DebugEventListener debugListener;

  public VmBase(DebugEventListener debugListener) {
    this.debugListener = debugListener;
  }

  @NotNull
  public AttachStateManager getAttachStateManager() {
    return this;
  }

  @Override
  public DebugEventListener getDebugListener() {
    return debugListener;
  }
}
