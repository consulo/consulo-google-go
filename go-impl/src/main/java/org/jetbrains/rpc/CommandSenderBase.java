package org.jetbrains.rpc;

import consulo.util.concurrent.AsyncResult;
import org.jetbrains.jsonProtocol.Request;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public abstract class CommandSenderBase<SUCCESS_RESPONSE> {
  protected abstract <RESULT> void doSend(Request<RESULT> message, RequestPromise<SUCCESS_RESPONSE, RESULT> callback);

  public <RESULT> AsyncResult<RESULT> send(Request<RESULT> message) {
    RequestPromise<SUCCESS_RESPONSE, RESULT> callback = new RequestPromise<>(message.getMethodName());
    doSend(message, callback);
    return callback;
  }
}
