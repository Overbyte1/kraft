package rpc;

import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.codec.FrameDecoder;
import rpc.codec.FrameEncoder;
import rpc.codec.ProtocolDecoder;
import rpc.codec.ProtocolEncoder;
import rpc.exception.NetworkException;
import rpc.handler.ServiceInboundHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理网络连接，包括连接的建立、关闭以及获取连接的Channel
 */
public class ChannelGroup {
    private static final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
    private Map<NodeId, NioChannel> channelMap;
    private final NodeId selfId;

    public ChannelGroup(NodeId selfId) {
        this.selfId = selfId;
        channelMap = new ConcurrentHashMap<>();
    }

    public NioChannel getOrConnect(NodeId nodeId, Endpoint endpoint) {
        NioChannel channel = channelMap.get(nodeId);
        if (channel != null) {
            return channel;
        }
        //建立连接
        channel = connect(endpoint.getIpAddress(), endpoint.getPort());

        addChannel(nodeId, channel);
        return channel;
        //TODO:处理异常
    }

    /**
     * 使用Map避免重复连接
     * TODO：重复连接存在问题：
     * 1. selfId发送失败
     * 2. 两个节点同时发起连接
     * @param ipAddress
     * @param port
     * @return
     */
    private NioChannel connect(String ipAddress, int port) {
        EventLoopGroup workerGroup = Instance.getWorkerGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(ipAddress, port)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new ProtocolEncoder());
                        //TODO:
                        pipeline.addLast(new ServiceInboundHandler());
                        pipeline.addLast(new IdentificationHandler());
                    }
                });
        try {
            logger.debug("connect to {}:{}", ipAddress, port);
            ChannelFuture future = bootstrap.connect().sync();
            return new NioChannel(future.channel());
        } catch (InterruptedException e) {
            throw new NetworkException("fail to connect " + ipAddress + ":" + port);
        }
    }

    /**
     * 静态内部类 单例
     */
    private static class Instance {
        private static EventLoopGroup workerGroup = new NioEventLoopGroup();
        static EventLoopGroup getWorkerGroup() {
            return workerGroup;
        }
    }
    /**
     * 在连接建立后发送自身NodeId，以帮助对端标识连接，在避免重复连接时也用到对端的NodeId
     */
    private class IdentificationHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().writeAndFlush(selfId);
            super.channelActive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            logger.warn("exception Caught: {}" + cause.getMessage());
            ctx.close();
//            super.exceptionCaught(ctx, cause);
        }
    }

    public void removeChannel(NodeId nodeId) {
        channelMap.remove(nodeId);
    }

    public synchronized void addChannel(NodeId nodeId, NioChannel channel) {
        if(channelMap.containsKey(nodeId)) {
            logger.info("connection of nodeId {} is exist", nodeId);
            return;
        }
        channelMap.put(nodeId, channel);
    }
}
