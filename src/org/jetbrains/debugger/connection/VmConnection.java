package org.jetbrains.debugger.connection;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.Consumer;
import com.intellij.util.EventDispatcher;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.socketConnection.ConnectionState;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import com.intellij.util.io.socketConnection.SocketConnectionListener;
import consulo.concurrency.Promises;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.debugger.DebugEventListener;
import org.jetbrains.debugger.Vm;

import javax.swing.event.HyperlinkListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author VISTALL
 * @since 06-May-17
 */
public abstract class VmConnection<T extends Vm> implements Disposable {
  private AtomicReference<ConnectionState> stateRef = new AtomicReference<>(new ConnectionState(ConnectionStatus.NOT_CONNECTED));
  private EventDispatcher<DebugEventListener> myDispatcher = EventDispatcher.create(DebugEventListener.class);

  private List<Consumer<ConnectionState>> connectionDispatcher = ContainerUtil.createLockFreeCopyOnWriteList();

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
        listener.consume(newState);
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

  @NotNull
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
