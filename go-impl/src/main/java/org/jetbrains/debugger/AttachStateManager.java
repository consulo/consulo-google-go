package org.jetbrains.debugger;

import com.intellij.openapi.util.AsyncResult;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public interface AttachStateManager {
  default AsyncResult<?> detach() {
    return AsyncResult.done(null);
  }

  default boolean isAttached() {
    return true;
  }
}