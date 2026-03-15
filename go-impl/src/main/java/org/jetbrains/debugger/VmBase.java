package org.jetbrains.debugger;

import consulo.util.dataholder.UserDataHolderBase;


/**
 * @author VISTALL
 * @since 08-May-17
 */
public class VmBase extends UserDataHolderBase implements Vm, AttachStateManager {
  private final DebugEventListener debugListener;

  public VmBase(DebugEventListener debugListener) {
    this.debugListener = debugListener;
  }

  public AttachStateManager getAttachStateManager() {
    return this;
  }

  @Override
  public DebugEventListener getDebugListener() {
    return debugListener;
  }
}
