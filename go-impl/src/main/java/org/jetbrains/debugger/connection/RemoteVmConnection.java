package org.jetbrains.debugger.connection;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Condition;
import com.intellij.util.io.NettyKt;
import com.intellij.util.io.socketConnection.ConnectionStatus;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import javax.annotation.Nonnull;
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

  public abstract Bootstrap createBootstrap(@Nonnull InetSocketAddress address, @Nonnull AsyncResult<Vm> vmResult);

  protected abstract String connectedAddressToPresentation(@Nonnull InetSocketAddress address, @Nonnull Vm vm);

  public AsyncResult<Vm> open(InetSocketAddress address) {
    return open(address, null);
  }

  public AsyncResult<Vm> open(InetSocketAddress address, Condition<Void> stopCondition) {
    myAddress = address;

    setState(ConnectionStatus.WAITING_FOR_CONNECTION, "Connecting to " + address.getHostString() + ":" + address.getPort());
    AsyncResult<Vm> result = new AsyncResult<>();

    AtomicInteger attemptNumber = new AtomicInteger();

    Runnable attempt = new Runnable() {
      @Override
      public void run() {
        connectCancelHandler.set(() -> result.reject("Closed explicitly"));
        AsyncResult<?> connectionPromise = new AsyncResult<>();
        connectionPromise.doWhenRejected(result::reject);

        result.doWhenDone(it -> {
          myVm = it;
          setState(ConnectionStatus.CONNECTED, "Connected to " + connectedAddressToPresentation(address, it));
          startProcessing();
        }).doWhenRejectedWithThrowable(it -> {
          if (!(it instanceof ConnectException)) {
            LOG.error(it);
          }

          setState(ConnectionStatus.CONNECTION_FAILED, it.getMessage());
        }).doWhenProcessed(() -> connectionPromise.setDone(null));

        Bootstrap bootstrap = createBootstrap(address, result);

        Channel channel =
                NettyKt.connect(bootstrap, address, connectionPromise, stopCondition == null ? NettyUtil.DEFAULT_CONNECT_ATTEMPT_COUNT : -1, stopCondition);

        if (channel != null) {
          ChannelFuture closeFuture = channel.closeFuture();
          if (closeFuture != null) {
            closeFuture.addListener(it -> {
              if (result.isDone()) {
                close("Process disconnected unexpectedly", ConnectionStatus.DISCONNECTED);
              }
              else if (attemptNumber.incrementAndGet() > 100 || (stopCondition != null && stopCondition.value(null))) {
                result.reject("Cannot establish connection - promptly closed after open");
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
        result.reject("Cancelled");
      }
    });
    return result;
  }

}

