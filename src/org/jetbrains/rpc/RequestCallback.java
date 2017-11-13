package org.jetbrains.rpc;

import java.io.IOException;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public interface RequestCallback<SUCCESS_RESPONSE> {
  void onSuccess(SUCCESS_RESPONSE response, ResultReader<SUCCESS_RESPONSE> resultReader);

  void onError(Throwable throwable);

  default void onError(String error) {
    onError(new IOException(error));
  }
}
