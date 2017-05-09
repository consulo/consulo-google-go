package org.jetbrains.debugger.connection;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Condition;
import com.intellij.util.io.NettyKt;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import consulo.concurrency.Promises;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.AsyncPromise;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.Vm;
import org.jetbrains.io.NettyUtil;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public abstract class RemoteVmConnection extends VmConnection<Vm> {
  private static final Logger LOG = Logger.getInstance(RemoteVmConnection.class);

  private AtomicReference<Runnable> connectCancelHandler = new AtomicReference<>();

  private InetSocketAddress myAddress;

  public abstract Bootstrap createBootstrap(@NotNull InetSocketAddress address, @NotNull AsyncPromise<Vm> vmResult);

  protected abstract String connectedAddressToPresentation(@NotNull InetSocketAddress address, @NotNull Vm vm);

  public Promise<Vm> open(InetSocketAddress address) {
    return open(address, null);
  }

  public Promise<Vm> open(InetSocketAddress address, Condition<Void> stopCondition) {
    myAddress = address;

    setState(ConnectionStatus.WAITING_FOR_CONNECTION, "Connecting to " + address.getHostString() + ":" + address.getPort());
    AsyncPromise<Vm> result = new AsyncPromise<>();

    AtomicInteger attemptNumber = new AtomicInteger();

    Runnable attempt = new Runnable() {
      @Override
      public void run() {
        connectCancelHandler.set(() -> result.setError("Closed explicitly"));
        AsyncPromise<?> connectionPromise = new AsyncPromise<>();
        connectionPromise.rejected(result::setError);

        result.done(it -> {
          myVm = it;
          setState(ConnectionStatus.CONNECTED, "Connected to " + connectedAddressToPresentation(address, it));
          startProcessing();
        }).rejected(it -> {
          if (!(it instanceof ConnectException)) {
            Promises.errorIfNotMessage(LOG, it);
          }

          setState(ConnectionStatus.CONNECTION_FAILED, it.getMessage());
        }).processed(it -> connectionPromise.setResult(null));

        Bootstrap bootstrap = createBootstrap(address, result);

        Channel channel =
                NettyKt.connect(bootstrap, address, connectionPromise, stopCondition == null ? NettyUtil.DEFAULT_CONNECT_ATTEMPT_COUNT : -1, stopCondition);

        if (channel != null) {
          ChannelFuture closeFuture = channel.closeFuture();
          if (closeFuture != null) {
            closeFuture.addListener(it -> {
              if (Promises.isFulfilled(result)) {
                close("Process disconnected unexpectedly", ConnectionStatus.DISCONNECTED);
              }
              else if (attemptNumber.incrementAndGet() > 100 || (stopCondition != null && stopCondition.value(null))) {
                result.setError("Cannot establish connection - promptly closed after open");
              }
              else {
                NettyKt.sleep(result, 500);
                run();
              }
            });
          }
        }
      }
    };

    Future<?> future = ApplicationManager.getApplication().executeOnPooledThread(() -> {
      if (Thread.interrupted()) {
        return;
      }

      attempt.run();
    });

    connectCancelHandler.set(() -> {
      try {
        future.cancel(true);
      }
      finally {
        result.setError("Cancelled");
      }
    });
    return result;
  }

}


