package org.jetbrains.rpc;

import consulo.util.concurrent.AsyncResult;
import org.jetbrains.jsonProtocol.Request;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public interface MessageProcessor {
  void cancelWaitingRequests();

  void closed();

  <RESULT> AsyncResult<RESULT> send(Request<RESULT> message);
}
