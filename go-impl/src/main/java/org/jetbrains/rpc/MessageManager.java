package org.jetbrains.rpc;

import consulo.logging.Logger;
import consulo.util.collection.ConcurrentIntObjectMap;
import consulo.util.collection.Maps;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public class MessageManager<REQUEST, INCOMING, INCOMING_WITH_SEQ, SUCCESS> extends MessageManagerBase {
  public interface Handler<OUTGOING, INCOMING, INCOMING_WITH_SEQ, SUCCESS> {
    int getUpdatedSequence(OUTGOING message);

    boolean write(OUTGOING message) throws IOException;

    INCOMING_WITH_SEQ readIfHasSequence(INCOMING incoming);

    default int getSequence(INCOMING_WITH_SEQ incomingWithSeq) {
      throw new AbstractMethodError();
    }

    default int getSequence(INCOMING_WITH_SEQ incomingWithSeq, INCOMING incoming) {
      return getSequence(incomingWithSeq);
    }

    void acceptNonSequence(INCOMING incoming);

    void call(INCOMING_WITH_SEQ response, RequestCallback<SUCCESS> callback);
  }

  private static final Logger LOG = Logger.getInstance(MessageManager.class);
  private ConcurrentIntObjectMap<RequestCallback<SUCCESS>> callbackMap = Maps.newConcurrentIntObjectHashMap();
  private MessageManager.Handler<REQUEST, INCOMING, INCOMING_WITH_SEQ, SUCCESS> handler;

  public MessageManager(Handler<REQUEST, INCOMING, INCOMING_WITH_SEQ, SUCCESS> handler) {
    this.handler = handler;
  }

  public void processIncoming(INCOMING incomingParsed) {
    INCOMING_WITH_SEQ commandResponse = handler.readIfHasSequence(incomingParsed);
    if (commandResponse == null) {
      if (closed) {
        // just ignore
        LOG.info("Connection closed, ignore incoming");
      }
      else {
        handler.acceptNonSequence(incomingParsed);
      }
      return;
    }

    RequestCallback<SUCCESS> callback = getCallbackAndRemove(handler.getSequence(commandResponse, incomingParsed));
    if (rejectIfClosed(callback)) {
      return;
    }

    try {
      handler.call(commandResponse, callback);
    }
    catch (Throwable e) {
      callback.onError(e);
      LOG.error("Failed to dispatch response to callback", e);
    }
  }

  @Nonnull
  public RequestCallback<SUCCESS> getCallbackAndRemove(int id) {
    RequestCallback<SUCCESS> callback = callbackMap.remove(id);

    if (callback == null) throw new IllegalArgumentException("Cannot find callback with id " + id);
    return callback;
  }

  public void send(REQUEST message, RequestCallback<SUCCESS> callback) {
    if (rejectIfClosed(callback)) {
      return;
    }

    int sequence = handler.getUpdatedSequence(message);
    callbackMap.put(sequence, callback);

    boolean success;
    try {
      success = handler.write(message);
    }
    catch (Throwable e) {
      try {
        failedToSend(sequence);
      }
      finally {
        LOG.error("Failed to send", e);
      }
      return;
    }

    if (!success) {
      failedToSend(sequence);
    }
  }

  private void failedToSend(int sequence) {
    RequestCallback<SUCCESS> removed = callbackMap.remove(sequence);
    if (removed != null) {
      removed.onError("Failed to send");
    }
  }

  public void cancelWaitingRequests() {
    // we should call them in the order they have been submitted
    ConcurrentIntObjectMap<RequestCallback<SUCCESS>> map = callbackMap;
    int[] keys = map.keys();
    Arrays.sort(keys);

    for (int key : keys) {
      RequestCallback<SUCCESS> successRequestCallback = map.get(key);

      if (successRequestCallback != null) {
        reject(successRequestCallback);
      }
    }
  }
}
