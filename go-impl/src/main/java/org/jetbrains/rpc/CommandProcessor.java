package org.jetbrains.rpc;

import org.jetbrains.jsonProtocol.Request;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public abstract class CommandProcessor<INCOMING, INCOMING_WITH_SEQ, SUCCESS_RESPONSE> extends CommandSenderBase<SUCCESS_RESPONSE>
        implements MessageManager.Handler<Request<?>, INCOMING, INCOMING_WITH_SEQ, SUCCESS_RESPONSE>, ResultReader<SUCCESS_RESPONSE>, MessageProcessor {

  private AtomicInteger currentSequence = new AtomicInteger();
  protected MessageManager<Request<?>, INCOMING, INCOMING_WITH_SEQ, SUCCESS_RESPONSE> messageManager = new MessageManager<>(this);

  @Override
  public int getUpdatedSequence(Request<?> message) {
    int id = currentSequence.incrementAndGet();
    message.finalize(id);
    return id;
  }

  @Override
  public void cancelWaitingRequests() {
    messageManager.cancelWaitingRequests();
  }

  @Override
  protected <RESULT> void doSend(Request<RESULT> message, RequestPromise<SUCCESS_RESPONSE, RESULT> callback) {
    messageManager.send(message, callback);
  }

  @Override
  public void closed() {
    messageManager.closed();
  }

  public MessageManager<Request<?>, INCOMING, INCOMING_WITH_SEQ, SUCCESS_RESPONSE> getMessageManager() {
    return messageManager;
  }
}
