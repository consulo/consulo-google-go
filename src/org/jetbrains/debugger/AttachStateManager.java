package org.jetbrains.debugger;

import consulo.concurrency.Promises;
import org.jetbrains.concurrency.Promise;

/**
 * @author VISTALL
 * @since 08-May-17
 */
public interface AttachStateManager {
  default Promise<?> detach() {
    return Promises.resolvedPromise();
  }

  default boolean isAttached() {
    return true;
  }
}
