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
import rpc.message.AbstractMessage;
import rpc.message.MessageType;
import rpc.message.RequestVoteMessage;

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
            AbstractMessage<RequestVoteMessage> message = new AbstractMessage<>(MessageType.RequestVote,
                    new RequestVoteMessage(0, new NodeId("123"), 0, 1));
            future.channel().writeAndFlush(message);
            Thread.sleep(10000);
            System.out.println("client send");
            future.channel().closeFuture();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }
}
