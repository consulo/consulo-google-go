package org.jetbrains.rpc;

import org.jetbrains.concurrency.Promise;
import org.jetbrains.jsonProtocol.Request;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public interface MessageProcessor {
  void cancelWaitingRequests();

  void closed();

  <RESULT> Promise<RESULT> send(Request<RESULT> message);
}
