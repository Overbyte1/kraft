package rpc;

import rpc.exception.NetworkException;
import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteResultMessage;

public class NioChannel implements rpc.Channel {
    private io.netty.channel.Channel nettyChannel;
//    private NodeId nodeId;

    public NioChannel(io.netty.channel.Channel channel) {
        nettyChannel = channel;
    }

    @Override
    public void writeRequestVoteMessage(RequestVoteResultMessage message) {
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

    @Override
    public void close() {
        try {
            //TODO:同步关闭连接？
            nettyChannel.close().sync();
        } catch (InterruptedException e) {
            throw new NetworkException("fail to close connection: " + nettyChannel);
        }
    }
}
