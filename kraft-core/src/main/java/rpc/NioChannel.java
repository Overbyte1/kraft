package rpc;

import io.netty.channel.ChannelFuture;
import rpc.exception.NetworkException;
import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

import java.util.Objects;

public class NioChannel implements rpc.Channel {
    private io.netty.channel.Channel nettyChannel;
//    private NodeId nodeId;

    public NioChannel(io.netty.channel.Channel channel) {
        nettyChannel = channel;
    }

    public ChannelFuture writeMessage(Object message) {
        return nettyChannel.writeAndFlush(message);
    }

    @Override
    public void writeRequestVoteMessage(RequestVoteMessage message) {
        nettyChannel.writeAndFlush(message);
    }

    @Override
    public void writeAppendEntriesMessage(AppendEntriesMessage message) {
        nettyChannel.writeAndFlush(message);
    }

    @Override
    public void writeRequestVoteResultMessage(RequestVoteResultMessage message) {
        nettyChannel.writeAndFlush(message);
    }

    @Override
    public void writeAppendEntriesResultMessage(AppendEntriesResultMessage message) {
        nettyChannel.writeAndFlush(message);
    }
    public boolean isActive() {
        return nettyChannel.isActive();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NioChannel channel = (NioChannel) o;
        return Objects.equals(nettyChannel, channel.nettyChannel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nettyChannel);
    }

    @Override
    public void close() {
        nettyChannel.close();
    }
}
