package org.jetbrains.debugger.connection;

import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.proxy.EventDispatcher;
import consulo.util.collection.Lists;
import consulo.util.concurrent.AsyncPromise;
import consulo.util.concurrent.Promises;
import consulo.util.socketConnection.ConnectionState;
import consulo.util.socketConnection.ConnectionStatus;
import consulo.util.socketConnection.SocketConnectionListener;
import org.jetbrains.debugger.DebugEventListener;
import org.jetbrains.debugger.Vm;

import jakarta.annotation.Nonnull;
import javax.swing.event.HyperlinkListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public abstract class VmConnection<T extends Vm> implements Disposable
{
  private AtomicReference<ConnectionState> stateRef = new AtomicReference<>(new ConnectionState(ConnectionStatus.NOT_CONNECTED));
  private EventDispatcher<DebugEventListener> myDispatcher = EventDispatcher.create(DebugEventListener.class);

  private List<Consumer<ConnectionState>> connectionDispatcher = Lists.newLockFreeCopyOnWriteList();

  private AsyncPromise<?> opened = new AsyncPromise<>();
  private AtomicBoolean closed = new AtomicBoolean();

  protected volatile T myVm;

  public ConnectionState getState() {
    return stateRef.get();
  }

  protected void setState(ConnectionStatus status) {
    setState(status, null, null);
  }

  protected void setState(ConnectionStatus status, String message) {
    setState(status, message, null);
  }

  protected void setState(ConnectionStatus status, String message, HyperlinkListener messageLinkListener) {
    ConnectionState newState = new ConnectionState(status, message, messageLinkListener);
    ConnectionState oldState = stateRef.getAndSet(newState);

    if (oldState == null || oldState.getStatus() != status) {
      if (status == ConnectionStatus.CONNECTION_FAILED) {
        opened.setError(newState.getMessage());
      }

      for (Consumer<ConnectionState> listener : connectionDispatcher) {
        listener.accept(newState);
      }
    }
  }

  protected void startProcessing() {
    opened.setResult(null);
  }

  public void addListener(SocketConnectionListener listener) {
    stateChanged(connectionState -> listener.statusChanged(connectionState.getStatus()));
  }

  public void stateChanged(Consumer<ConnectionState> listener) {
    connectionDispatcher.add(listener);
  }

  public void addDebugListener(DebugEventListener listener) {
    myDispatcher.addListener(listener);
  }

  @Nonnull
  public DebugEventListener getDebugEventListener() {
    return myDispatcher.getMulticaster();
  }

  public void close(String message, ConnectionStatus status) {
    if (!closed.compareAndSet(false, true)) {
      return;
    }

    if (Promises.isPending(opened)) {
      opened.setError("closed");
    }
    setState(status, message);
    Disposer.dispose(this, false);
  }

  public T getVm() {
    return myVm;
  }

  @Override
  public void dispose() {
    myVm = null;
  }
}
