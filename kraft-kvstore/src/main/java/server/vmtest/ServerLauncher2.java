package server.vmtest;

import com.alibaba.fastjson.JSON;
import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.command.*;
import config.ClusterConfig;
import election.node.Node;
import election.node.NodeImpl;
import election.statemachine.DefaultStateMachine;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import server.KVDatabase;
import server.KVDatabaseImpl;
import server.ServerLauncher;

import server.config.ServerConfig;
import server.config.ServerConfigLoader;
import server.handler.*;
import server.store.KVStore;
import server.store.MemHTKVStore;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CyclicBarrier;

public class ServerLauncher2 {
    private Node node;
    private KVDatabase kvDatabase;
    private Channel channel;
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    private TestHandle testHandle = new TestHandle(cyclicBarrier);
    private KVStore kvStore = new MemHTKVStore();

    private void buildNode() throws IOException {
        ClusterConfig config = JSON.parseObject(new FileInputStream("./kraft-kvstore/conf/raft2.json"), ClusterConfig.class);
        System.out.println(config);
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.withId("B")
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withPath(config.getPath() + "B/")
                .withStateMachine(new DefaultStateMachine())
                .build();
    }

    public void init() throws IOException {
        buildNode();
        ServerConfig config = JSON.parseObject(new FileInputStream("./kraft-kvstore/conf/server2.json"), ServerConfig.class);

        kvDatabase = new KVDatabaseImpl(node, config);

        kvDatabase.start();
        kvDatabase.registerCommandHandler(GetCommand.class, new GetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(SetCommand.class, new SetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(DelCommand.class, new DelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MDelCommand.class, new MDelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MSetCommand.class, new MSetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MGetCommand.class, new MGetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(LeaderCommand.class, new LeaderCommandHandler(node));
        kvDatabase.registerCommandHandler(ServerListCommand.class, new ServerListCommandHandler(node));
        kvDatabase.registerCommandHandler(PingCommand.class, new PingCommandHandler());
    }
    public static void main(String[] args) throws IOException {
        ServerLauncher2 launcher = new ServerLauncher2();
        launcher.init();
    }
}

//class TestHandle extends ChannelInboundHandlerAdapter {
//    private Object receiveMessage;
//    private CyclicBarrier cyclicBarrier;
//
//    public TestHandle(CyclicBarrier cyclicBarrier) {
//        this.cyclicBarrier = cyclicBarrier;
//    }
//
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("client receive message: " + msg);
//        receiveMessage = msg;
//        cyclicBarrier.await();
//        super.channelRead(ctx, msg);
//    }
//
//    public Object getReceiveMessage() {
//        return receiveMessage;
//    }
//    public void resetMsg() {
//        receiveMessage = null;
//    }
//}
