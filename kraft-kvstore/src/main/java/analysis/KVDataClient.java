package analysis;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.rocksdb.RocksDBException;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class KVDataClient {
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    public static void main(String[] args) throws RocksDBException {
        int port = 9981;
//        Options options = new Options();
//
//        options.setCreateIfMissing(true);
//        KVStore kvStore = new RocksDBTransactionKVStore(options, "./db/A/");
//        KVDataCollector collector = new KVDataCollector(kvStore, port);
//        collector.start();
        KVDataClient client = new KVDataClient();
        client.handle("localhost", port);

    }
    public void handle(String ip, int port) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ReadHandler handler = new ReadHandler();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new ProtocolEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(handler);
                    }
                });
        try {
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            future.channel().writeAndFlush(AnalysisType.ALL_KV_DATA);
            cyclicBarrier.await();
            System.out.println(handler.getMsg());
//            Map<byte[], byte[]> map = (Map<byte[], byte[]>) handler.getMsg();
//            String gap = "\t\t\t\t\t\t\t";
//            System.out.println("key" + gap + "value");
//            System.out.println("-----------------------------------------------------");
//            for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
//                System.out.println(new String(entry.getKey()) + gap + new String(entry.getValue()));
//            }
            workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

    }
    private class ReadHandler extends ChannelInboundHandlerAdapter {
        private Object msg;
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.msg = msg;
            cyclicBarrier.await(2000, TimeUnit.MILLISECONDS);
            super.channelRead(ctx, msg);
        }
        public Object getMsg() {
            return msg;
        }
    }
}
