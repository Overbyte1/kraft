package analysis;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class AnalysisServer {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisServer.class);

    private final int port;
    private volatile boolean start;
    private final Map<Integer, Collector> collectorMap = new HashMap<>();

    public AnalysisServer(int port) {
        this.port = port;
        this.start = false;
    }

    public void registerCollector(int type, Collector collector) {
        collectorMap.put(type, collector);
    }
    public void unregisterCollector(int type) {
        collectorMap.remove(type);
    }

    public void start() {
//        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
//        ServerBootstrap bootstrap = new ServerBootstrap();
//        bootstrap.group(workerGroup)
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInitializer<NioSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioSocketChannel ch) throws Exception {
//                        ChannelPipeline pipeline = ch.pipeline();
//                        pipeline.addLast(new Encoder());
//                        pipeline.addLast(new Decoder());
//                        pipeline.addLast(new CollectorHandler());
//                    }
//                });
//        try {
//            bootstrap.bind(port).sync();
//        } catch (InterruptedException e) {
//            workerGroup.shutdownGracefully();
//            e.printStackTrace();
//        }
        start = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(start) {


                Socket socket = serverSocket.accept();

                InputStream inputStream = socket.getInputStream();
                int type = inputStream.read();
                logger.debug("analysis server receive type: {}", type);
                Collector collector = collectorMap.get(type);
                String result = collector.collect();
                OutputStream os = socket.getOutputStream();
                byte[] bytes = result.getBytes();
                os.write(bytes.length);
                os.write(bytes);
                os.flush();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public void stop() {
        start = false;
    }
    private class CollectorHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof Integer) {
                Collector collector = collectorMap.get((Integer) msg);
                if(collector != null) {
                    ctx.channel().writeAndFlush(collector.collect());
                } else {
                    logger.warn("collector not found, msg: {}", msg);
                }
//                Map<byte[], byte[]> map = new HashMap<>();
//                try (final KVStoreIterator iterator = kvStore.newIterator()) {
//                    iterator.seekToFirst();
//                    for (; iterator.isValid(); iterator.next()) {
//                        map.put(iterator.key(), iterator.value());
//                    }
//                }
//                ctx.channel().writeAndFlush(map);
            } else {
                logger.warn("receive unknown message: {}", msg);
                super.channelRead(ctx, msg);
            }
        }
    }

}
