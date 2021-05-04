package analysis;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import server.store.KVStore;
import server.store.KVStoreIterator;

import java.util.HashMap;
import java.util.Map;

public class KVDataCollector {
    private KVStore kvStore;
    private final int port;

    public KVDataCollector(KVStore kvStore, int port) {
        this.kvStore = kvStore;
        this.port = port;
    }
    public void start() {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new ProtocolEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new CollectorHandler());
                    }
                });
        try {
            bootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }
    private class CollectorHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof GetAllDataCommand) {
                Map<byte[], byte[]> map = new HashMap<>();
                try (final KVStoreIterator iterator = kvStore.newIterator()) {
                    iterator.seekToFirst();
                    for (; iterator.isValid(); iterator.next()) {
                        map.put(iterator.key(), iterator.value());
                    }
                }
                ctx.channel().writeAndFlush(map);
            } else {
                super.channelRead(ctx, msg);
            }
        }
    }

}
