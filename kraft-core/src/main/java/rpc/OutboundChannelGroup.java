package rpc;

import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.exception.NetworkException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理出站连接
 */
public class OutboundChannelGroup {
    private NodeId selfId;
    private EventLoopGroup workerGroup;
    private Map<NodeId, NioChannel> channelMap;

    public OutboundChannelGroup(NodeId selfId) {
        this.selfId = selfId;
        workerGroup = new NioEventLoopGroup();
        channelMap = new ConcurrentHashMap<>();
    }

    public NioChannel getOrConnect(NodeId nodeId, Endpoint endpoint) {
        NioChannel channel = channelMap.get(nodeId);
        if(channel != null) {
            return channel;
        }
        //建立连接
        channel = connect(endpoint.getIpAddress(), endpoint.getPort());
        channelMap.put(nodeId, channel);
        return channel;
        //TODO:处理异常
    }
    private NioChannel connect(String ipAddress, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(ipAddress, port)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //TODO:添加encoder和decoder
                        //pipeline.addLast()
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect().sync();
            return new NioChannel(future.channel());
        } catch (InterruptedException e) {
            throw new NetworkException("fail to connect " + ipAddress + ":" + port);
        }
    }
    public void removeChannel(NodeId nodeId) {
        channelMap.remove(nodeId);
    }
    public void addChannel(NodeId nodeId, NioChannel channel) {
        channelMap.put(nodeId, channel);
    }
}
