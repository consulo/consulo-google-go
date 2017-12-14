package org.jetbrains.debugger;

import io.netty.channel.Channel;
import org.jetbrains.rpc.MessageProcessor;

/**
 * @author VISTALL
 * @since 09-May-17
 */
public class StandaloneVmHelper implements AttachStateManager {
  private Vm myVm;
  private MessageProcessor myMessageProcessor;
  private Channel myChannel;

  public StandaloneVmHelper(Vm vm, MessageProcessor messageProcessor, Channel channel) {
    myVm = vm;
    myMessageProcessor = messageProcessor;
    myChannel = channel;
  }

  @Override
  public boolean isAttached() {
    return myChannel != null;
  }

  public boolean write(Object content) {
    Channel channel = getChannelIfActive();
    return channel != null && !channel.writeAndFlush(content).isCancelled();
  }

  Channel getChannelIfActive() {
    Channel currentChannel = myChannel;
    return currentChannel == null || !currentChannel.isActive() ? null : currentChannel;
  }

}
