package rpc;

import election.node.NodeId;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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

public class RpcHandlerImpl implements RpcHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcHandlerImpl.class);
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelGroup channelGroup;
    private static final long DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final long DEFAULT_SEND_TIMEOUT = 3000;

    private final int port;
    private long connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private long sendTimeout = DEFAULT_SEND_TIMEOUT;
    private NodeId selfId;

//    private final int NCPU = Runtime.getRuntime().availableProcessors();
//    private final int taskQueueSize = 10000;
//    private final long keepAliveTime = 100;
//
//    private final Executor executor = new ThreadPoolExecutor(NCPU, NCPU * 2, keepAliveTime,
//            TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(taskQueueSize));

    public RpcHandlerImpl(ChannelGroup channelGroup, int port, long connectTimeout, NodeId selfId) {
        this.channelGroup = channelGroup;
        this.port = port;
        this.connectTimeout = connectTimeout;
        this.selfId = selfId;
        //initialize();
    }
    public RpcHandlerImpl(ChannelGroup channelGroup, int port) {
        this.channelGroup = channelGroup;
        this.port = port;
        this.selfId = channelGroup.getSelfId();
    }

    @Override
    public void initialize() {
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
            //e.printStackTrace();
        }
    }

    @Override
    public void sendRequestVoteMessage(RequestVoteMessage message, Collection<NodeEndpoint> nodeEndpoints) {
        //RequestVoteMessage message = new RequestVoteMessage(term, candidateId, lastLogIndex, lastLogTerm);
        //发送给其他所有节点
        for (NodeEndpoint nodeEndpoint : nodeEndpoints) {
            if(!selfId.equals(nodeEndpoint.getNodeId())) {
                sendMessage(nodeEndpoint, message);
            }
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
            if(!selfId.equals(nodeEndpoint.getNodeId())) {
                sendMessage(nodeEndpoint, message);
            }
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
//    private void sendMessage(NodeEndpoint nodeEndpoint, Object message) {
//        executor.execute(()->{doSendMessage(nodeEndpoint, message);});
//    }
    private void sendMessage(NodeEndpoint nodeEndpoint, Object message) {
        NodeId nodeId = nodeEndpoint.getNodeId();
        try {
            if(channelGroup.isConnect(nodeId)) {
                channelGroup.writeMessage(nodeId, message, sendTimeout);
                return;
            }
            channelGroup.connectAndWriteMessage(nodeEndpoint, message, connectTimeout, sendTimeout);
        } catch (Exception exception) {
            logger.warn("fail to send message to node {}, endpoint is {}, cause is {}",
                    nodeId, nodeEndpoint.getEndpoint(), exception.getMessage());
            exception.printStackTrace();
        }
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
