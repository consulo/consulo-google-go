package org.jetbrains.rpc;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public abstract class MessageManagerBase {
  public static final String COLLECTION_CLOSED = "Collection closed";

  protected static void reject(RequestCallback<?> callback) {
    callback.onError(COLLECTION_CLOSED);
  }

  protected volatile boolean closed;

  protected boolean rejectIfClosed(RequestCallback<?> callback) {
    if (closed) {
      callback.onError(COLLECTION_CLOSED);
      return true;
    }

    return false;
  }

  public void closed() {
    closed = true;
  }
}
