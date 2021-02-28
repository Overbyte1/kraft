package rpc;

import election.config.GlobalConfig;
import election.node.NodeId;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.codec.FrameDecoder;
import rpc.codec.FrameEncoder;
import rpc.codec.ProtocolDecoder;
import rpc.codec.ProtocolEncoder;
import rpc.handler.IdentificationHandler;
import rpc.handler.ServiceInboundHandler;
import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class RpcHandlerImpl implements RpcHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImpl.class);
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelGroup channelGroup;

    private int port;

    public RpcHandlerImpl(ChannelGroup channelGroup, int port) {
        this.channelGroup = channelGroup;
        this.port = port;
        //initialize();
    }

    @Override
    public void initialize() {
        ServiceInboundHandler serviceInboundHandler = ServiceInboundHandler.getInstance();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //.childOption(NioChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new ProtocolEncoder());

                        //TODO:remove it
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));

                        pipeline.addLast(new IdentificationHandler(channelGroup));
                        pipeline.addLast(ServiceInboundHandler.getInstance());
                    }
                });
        try {
            serverBootstrap.bind(port).sync();
            logger.debug("succeed to bind port {}", port);
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendRequestVoteMessage(RequestVoteMessage message, Collection<NodeEndpoint> nodeEndpoints) {
        //RequestVoteMessage message = new RequestVoteMessage(term, candidateId, lastLogIndex, lastLogTerm);
        //发送给其他所有节点
        //TODO:异步发送，避免因为一个连接出现问题导致阻塞从而无法发送消息给其他节点，并且需要设置超时时间
        for (NodeEndpoint nodeEndpoint : nodeEndpoints) {
            sendMessage(nodeEndpoint, message);
        }

    }

    @Override
    public void sendAppendEntriesMessage(AppendEntriesMessage message, NodeEndpoint nodeEndpoint) {
        sendMessage(nodeEndpoint, message);
    }

    @Override
    public void sendAppendEntriesMessage(AppendEntriesMessage message, Collection<NodeEndpoint> nodeEndpoints) {
        //AppendEntriesMessage message = new AppendEntriesMessage(term, leaderId, preLogTerm, preLogIndex, logEntryList);
        for (NodeEndpoint nodeEndpoint : nodeEndpoints) {
            sendMessage(nodeEndpoint, message);
        }
    }

    @Override
    public void sendRequestVoteResultMessage(RequestVoteResultMessage message, NodeEndpoint nodeEndpoint) {
        //RequestVoteResultMessage message = new RequestVoteResultMessage(term, voteGranted);
        sendMessage(nodeEndpoint, message);
    }

    @Override
    public void sendAppendEntriesResultMessage(AppendEntriesResultMessage message, NodeEndpoint nodeEndpoint) {
        //AppendEntriesResultMessage message = new AppendEntriesResultMessage(term, success);
        sendMessage(nodeEndpoint, message);
    }
    private void sendMessage(NodeEndpoint nodeEndpoint, Object message) {
        NodeId nodeId = nodeEndpoint.getNodeId();
        try {
            if(channelGroup.isConnect(nodeId)) {
                channelGroup.writeMessage(nodeId, message);
                return;
            }
            //TODO:remove ti
            GlobalConfig config = new GlobalConfig();
            channelGroup.connectAndWriteMessage(nodeEndpoint, message, config.getConnectTimeout(), TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            logger.warn("fail to send message to node {}, endpoint is {}, cause is {}",
                    nodeId, nodeEndpoint.getEndpoint(), exception.getMessage());
            exception.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }

    public EventLoopGroup getBossGroup() {
        return bossGroup;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public int getPort() {
        return port;
    }
}
