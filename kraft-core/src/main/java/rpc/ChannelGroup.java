package rpc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.codec.FrameDecoder;
import rpc.codec.FrameEncoder;
import rpc.codec.ProtocolDecoder;
import rpc.codec.ProtocolEncoder;
import rpc.exception.NetworkException;
import rpc.handler.ServiceInboundHandler;
import rpc.message.AbstractMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 管理网络连接，包括连接的建立、关闭以及获取连接的Channel，全局唯一
 */
public class ChannelGroup {
    private static final Logger logger = LoggerFactory.getLogger(ChannelGroup.class);
    private Map<NodeId, NioChannel> channelMap;
    private Map<NioChannel, NodeId> nodeIdMap;
    private final NodeId selfId;

    public ChannelGroup(NodeId selfId) {
        this.selfId = selfId;
        //channelMap = new ConcurrentHashMap<>();
        //BiMap 双向映射
        BiMap<NodeId, NioChannel> map = HashBiMap.create();
        channelMap = map;
        nodeIdMap = map.inverse();
    }
    //TODO:优化锁性能
    public synchronized NodeId getNodeId(NioChannel channel) {
        return nodeIdMap.get(channel);
    }


    public synchronized NioChannel getChannel(NodeId nodeId) {
        return channelMap.get(nodeId);
    }

    public boolean isConnect(NodeId nodeId) {
        NioChannel channel = channelMap.get(nodeId);
        return channel != null && channel.isActive();
    }
    public void connectAndWriteMessage(NodeEndpoint nodeEndpoint, Object message, long connectTimeout, TimeUnit timeUnit) {
        NodeId nodeId = nodeEndpoint.getNodeId();
        if(isConnect(nodeId)) {
            NioChannel channel = getChannel(nodeId);
            //channel.writeMessage(message);
            writeMessage(nodeId, message);
            return;
        }
        Endpoint endpoint = nodeEndpoint.getEndpoint();
        ChannelFuture channelFuture = connect(endpoint.getIpAddress(), endpoint.getPort());
        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                //TODO:需要等待发送selfId后才能发送消息
                future.await(connectTimeout, timeUnit);
                if(future.isSuccess()) {
                    logger.debug("succeed to connect node {}, address: {}", nodeId, endpoint);
                    addChannel(nodeId, new NioChannel(channelFuture.channel()));
                    writeMessage(nodeId, message);
                } else {
                    logger.info("connect timeout, remote node is {}, endpoint is {}, cancel connection", nodeId, endpoint);
                    future.cancel(true);
                }
            }
        });
    }
    public void writeMessage(NodeId nodeId, Object message) {
        AbstractMessage abstractMessage = new AbstractMessage(0, selfId, message);
        NioChannel channel = channelMap.get(nodeId);
        if(isConnect(nodeId)) {
            channel.writeMessage(abstractMessage);
            logger.debug("send {}", abstractMessage);
        } else {
            removeChannel(nodeId);
            logger.warn("failed to write message, because the connection of node {} was closed", nodeId);
        }
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
    private ChannelFuture connect(String ipAddress, int port) {
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
                        pipeline.addLast(ServiceInboundHandler.getInstance());
                        pipeline.addLast(new IdentificationHandler());
                        //pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            logger.debug("try async connect to {}:{}", ipAddress, port);
            //ChannelFuture future = bootstrap.connect().sync();
            return bootstrap.connect();
//            future.addListener(new GenericFutureListener<Future<? super Void>>() {
//                @Override
//                public void operationComplete(Future<? super Void> future) throws Exception {
//                    future.await(2000);
//                    if (future.isSuccess()) {
//                        logger.debug("build connection");
//                    }
//                }
//            });
            //return new NioChannel(future.channel());
        } catch (Exception e) {
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
            cause.printStackTrace();
            ctx.close();
            //TODO:从map中移除channel
//            super.exceptionCaught(ctx, cause);
        }
    }

    public void removeChannel(NodeId nodeId) {
        NioChannel channel = channelMap.get(nodeId);
        if(channel != null) {
            if(channel.isActive()) {
                channel.close();
            }
            channelMap.remove(nodeId);
        }
    }

    public synchronized void addChannel(NodeId nodeId, NioChannel channel) {
        if(channelMap.containsKey(nodeId)) {
            logger.info("connection of nodeId {} is exist", nodeId);
            return;
        }
        if(channel != null)
            channelMap.put(nodeId, channel);
    }

}
