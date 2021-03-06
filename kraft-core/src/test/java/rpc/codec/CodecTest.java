package rpc.codec;

import election.node.NodeId;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.handler.ServiceInboundHandler;
import rpc.message.RequestVoteMessage;

import java.util.ArrayList;
import java.util.List;

public class CodecTest {
    @Test
    public void testCodecServer() throws InterruptedException {
        Thread serverThread = new Thread(this::server);
        serverThread.start();

        serverThread.join();
        Thread.sleep(10000000);

    }
    @Test
    public void testCodecClient() throws InterruptedException {
        Thread clientThread = new Thread(this::client);

        clientThread.start();
        clientThread.join();

    }
    public void server() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //.localAddress(8090)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new ProtocolEncoder());
                        pipeline.addLast(ServiceInboundHandler.getInstance());
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            System.out.println("server start");
            ChannelFuture future = serverBootstrap.bind(8090).sync();
            future.channel().closeFuture();
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }
    private void client() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress("127.0.0.1", 8090)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new ProtocolEncoder());
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect().sync();
//            AbstractMessage<RequestVoteMessage> message = new AbstractMessage<>(MessageType.RequestVote,
//                    new RequestVoteMessage(0, new NodeId("123"), 0, 1));
            RequestVoteMessage message = new RequestVoteMessage(0, new NodeId("123"), 0, 1);
            future.channel().writeAndFlush(message);
            Thread.sleep(10000);
            System.out.println("client send");
            future.channel().closeFuture();
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }
    private void test(String... args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }
    @Test
    public void testArg() {
        test();
        test("ddd");
        test("abc", "aaa");
        Logger logger = LoggerFactory.getLogger(this.getClass());

    }
    @Test
    public void testSubList() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        List<Integer> subList = list.subList(1, 1);
        System.out.println(subList.size());

    }

}
