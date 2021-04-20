package server;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.Connection;
import common.message.command.ModifiedCommand;
import common.message.response.*;
import config.ClusterConfig;
import election.node.Node;
import election.statemachine.StateMachine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.NodeEndpoint;
import server.config.ServerConfig;
import server.handler.CommandHandler;
import server.store.KVStore;
import server.store.MemHTKVStore;
import utils.SerializationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class KVDatabaseImpl implements KVDatabase {
    private static final Logger logger = LoggerFactory.getLogger(KVDatabaseImpl.class);
    /*
    Node需要提供的接口：
    1. 判断当前节点是否为Leader，决定是否要重定向到Leader
    2. 获取Leader的地址信息，返回客户端进行重定向

     */
    private final Node node;
    private final Map<String, Connection> connectorMap = new ConcurrentHashMap<>();
    private final Map<String, Future<?>> futureMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, CommandHandler> handlerMap = new HashMap<>();
    private final StateMachine stateMachine = new DefaultStateMachine();

    private final int NCPU = Runtime.getRuntime().availableProcessors() * 2;

    private final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(NCPU,
            (Runnable r)-> new Thread(r, "TimeoutTaskThread"));

    //private final KVStore kvStore;

    private final ServerConfig config;


    //    private class CleanTask implements Runnable{
//        final String requestId;
//
//        public CleanTask(String requestId) {
//            this.requestId = requestId;
//        }
//
//        @Override
//        public void run() {
//            connectorMap.remove(requestId);
//        }
//    }
    private class TimeoutResponseTask implements Runnable {
        final String requestId;

        public TimeoutResponseTask(String requestId) {
            this.requestId = requestId;
        }

        @Override
        public void run() {
            connectorMap.get(requestId)
                    .reply(new Response(ResponseType.FAILURE, new NoPayloadResult(StatusCode.FAIL_TIMEOUT)));
            connectorMap.remove(requestId);
            logger.warn("execution timeout, response client");
        }
    }

    public KVDatabaseImpl(Node node, ServerConfig config) {
        this.node = node;
        this.config = config;
        node.registerStateMachine(stateMachine);
    }

    @Override
    public void start() {

        KVDatabaseImpl kvStore = this;
        node.start();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder())
                                .addLast(new FrameEncoder())
                                .addLast(new ProtocolDecoder())
                                .addLast(new ProtocolEncoder())
                                .addLast(new ServiceHandler(kvStore))
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            serverBootstrap.bind(config.getPort()).sync();
            logger.info("server was started");
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.error("fail to start server, cause is: {}", e.getMessage());
            //e.printStackTrace();
        }

    }

    @Override
    public void stop() {
        logger.info("server was stopped");
    }

    private void redirectOrFail(Connection connection) {
        //TODO:添加选项，决定是否能够在Follower节点读数据
        NodeEndpoint leaderNodeEndpoint = node.getLeaderNodeEndpoint();
        if(leaderNodeEndpoint != null) {
            connection.reply(new Response(ResponseType.REDIRECT, new RedirectResult(leaderNodeEndpoint)));
        } else {
            //Leader不存在
            connection.reply(new Response(ResponseType.FAILURE, FailureResult.NO_LEADER));
        }
    }


    @Override
    public void handleCommand(Connection connection) {
        if(!node.isLeader()) {
            redirectOrFail(connection);
            return;
        }
        Object command = connection.getCommand();
        if(command instanceof ModifiedCommand) {
            String requestId = ((ModifiedCommand) command).getRequestId();
            connectorMap.put(requestId, connection);
            ScheduledFuture<?> future = scheduledExecutor.schedule(new TimeoutResponseTask(requestId),
                    config.getExecuteTimeout(), TimeUnit.MILLISECONDS);

            futureMap.put(requestId, future);
        }
        Response<?> response = handlerMap.get(command.getClass()).handleCommand(command);
        if(response != null) {
            connection.reply(response);
        }
    }

    @Override
    public void registerCommandHandler(Class<?> clazz, CommandHandler handler) {
        handlerMap.put(clazz, handler);
    }

    @Override
    public void unregisterCommandHandler(Class<?> clazz) {
        handlerMap.remove(clazz);
    }

    private class DefaultStateMachine implements StateMachine {
        @Override
        public boolean apply(byte[] command) {
            Object obj;
            try {
                obj = SerializationUtil.decode(command);
            } catch (Exception e) {
                logger.warn("fail to decode bytes: {}", e.getMessage());
                return false;
            }
            ModifiedCommand modifiedCommand = (ModifiedCommand) obj;
            String requestId = modifiedCommand.getRequestId();
            Response<?> response = handlerMap.get(modifiedCommand.getClass()).doHandle(modifiedCommand);
            try {
                connectorMap.get(requestId).reply(response);
                connectorMap.remove(requestId);
                futureMap.get(requestId).cancel(false);
                futureMap.remove(requestId);
            } catch (Exception exception) {
                logger.warn("fail to response client, because channel was not found. requestId is: {}", requestId);
            }
            return true;
        }

    }

}
