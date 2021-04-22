package server;

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
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft2.json"), ClusterConfig.class);
        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        node = builder.withId("B")
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withMemLogStore()
                .withStateMachine(new DefaultStateMachine())
                .build();
    }

    public void init() throws IOException {
        buildNode();
        kvDatabase = new KVDatabaseImpl(node, new ServerConfigLoader().load(null));
        kvDatabase.start();
        kvDatabase.registerCommandHandler(GetCommand.class, new GetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(SetCommand.class, new SetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(DelCommand.class, new DelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MDelCommand.class, new MDelCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MSetCommand.class, new MSetCommandHandler(kvStore, node));
        kvDatabase.registerCommandHandler(MGetCommand.class, new MGetCommandHandler(kvStore));
        kvDatabase.registerCommandHandler(LeaderCommand.class, new LeaderCommandHandler(node));
        kvDatabase.registerCommandHandler(ServerListCommand.class, new ServerListCommandHandler(node));

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress("localhost", 8848)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder())
                                .addLast(new FrameEncoder())
                                .addLast(new ProtocolDecoder())
                                .addLast(new ProtocolEncoder())
                                .addLast(testHandle)
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect().sync();
            System.out.println("connect established");
            channel = future.channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws IOException {
        ServerLauncher launcher = new ServerLauncher();
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
