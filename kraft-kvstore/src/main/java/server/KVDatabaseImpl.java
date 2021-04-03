package server;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.*;
import common.message.command.DelCommand;
import common.message.command.GetCommand;
import common.message.command.SetCommand;
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
import server.store.KVStore;
import utils.SerializationUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KVDatabaseImpl implements KVDatabase {
    private static final Logger logger = LoggerFactory.getLogger(KVDatabaseImpl.class);
    /*
    Node需要提供的接口：
    1. 判断当前节点是否为Leader，决定是否要重定向到Leader
    2. 获取Leader的地址信息，返回客户端进行重定向

     */
    private Node node;
    //private Map<String, byte[]> kvStore = new HashMap<>();
    private Map<String, Connection> connectorMap = new ConcurrentHashMap<>();
    private StateMachine stateMachine = new DefaultStateMachine();

    private KVStore kvStore;

    //TODO：配置
    private final int port = 8848;

    public KVDatabaseImpl(Node node, KVStore kvStore) {
        this.node = node;
        this.kvStore = kvStore;
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
                    protected void initChannel(NioSocketChannel ch) throws Exception {
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
            serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        logger.debug("server started");
    }

    @Override
    public void stop() {

    }

    private void redirectOrFail(Connection connection) {
        NodeEndpoint leaderNodeEndpoint = node.getLeaderNodeEndpoint();
        if(leaderNodeEndpoint != null) {
            connection.reply(new Response(ResponseType.REDIRECT, new RedirectResult(leaderNodeEndpoint)));
        } else {
            //Leader不存在
            connection.reply(new Response(ResponseType.FAILURE, Failure.NO_LEADER));
        }
    }

    //处理get命令
    @Override
    public void handleGetCommand(Connection<GetCommand> connection) {
        /*
        1. 判断当前节点是否是Leader，如果是则从Map中获取数据返回，否则
        2. 获取Leader的地址信息返回重定向消息，如果不存在Leader则返回错误
         */
        if(!node.isLeader()) {
            redirectOrFail(connection);
            return;
        }
        byte[] bytes = kvStore.get(connection.getCommand().getKey());
        logger.debug("Get operation: {}/{}", connection.getCommand().getKey(), bytes);
        connection.reply(new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK, bytes)));
    }

    //处理set命令
    @Override
    public void handleSetCommand(Connection<SetCommand> connection) throws IOException {
        if(!node.isLeader()) {
            redirectOrFail(connection);
            return;
        }
        SetCommand command = connection.getCommand();
        connectorMap.put(command.getRequestId(), connection);
        logger.debug("append key/value: [{}/{}] to log", command.getKey(), new String(command.getValue()));
        node.appendLog(SerializationUtil.encodes(command));
    }

    //处理del命令
    @Override
    public void handleDelCommand(Connection<DelCommand> connection) throws IOException {
        if(!node.isLeader()) {
            redirectOrFail(connection);
            return;
        }
        DelCommand command = connection.getCommand();
        //TODO：remove掉，处理内存泄漏问题
        connectorMap.put(command.getRequestId(), connection);
        String key = command.getKey();
        if(!kvStore.containsKey(key)) {
            connection.reply(new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_NO_CONTENT)));
        } else {
            logger.debug("delete key: [{}] to log", key);
            node.appendLog(SerializationUtil.encodes(command));
        }
    }

    private void doSet(SetCommand setCommand) {
        kvStore.set(setCommand.getKey(), setCommand.getValue());
        logger.debug("key/value [{}/{}] was set", setCommand.getKey(), new String(setCommand.getValue()));
        connectorMap.get(setCommand.getRequestId()).reply(new Response(ResponseType.SUCCEED,
                new GeneralResult(StatusCode.SUCCEED_OK)));
        connectorMap.remove(setCommand.getRequestId());
    }
    private void doDel(DelCommand delCommand) {
        kvStore.del(delCommand.getKey());
        logger.debug("key [{}] was deleted", delCommand.getKey());
        connectorMap.get(delCommand.getRequestId()).reply(new Response(ResponseType.SUCCEED,
                new GeneralResult(StatusCode.SUCCEED_OK)));
        connectorMap.remove(delCommand.getRequestId());
    }

    private class DefaultStateMachine implements StateMachine {
        @Override
        public boolean apply(byte[] command) {
            Object obj = null;
            try {
                obj = SerializationUtil.decode(command);
            } catch (Exception e) {
                logger.debug("fail to decode bytes: {}", e.getMessage());
                return false;
            }
            String requestId = null;
            if(obj instanceof SetCommand) {
                SetCommand setCommand = (SetCommand) obj;
                doSet(setCommand);
                requestId = setCommand.getRequestId();
            } else if(obj instanceof DelCommand) {
                DelCommand delCommand = (DelCommand)obj;
                doDel(delCommand);
                requestId = delCommand.getRequestId();
            }
            return true;
        }

    }

}
