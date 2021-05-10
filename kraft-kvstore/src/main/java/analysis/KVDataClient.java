package analysis;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class KVDataClient {
    private CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    public static void main(String[] args) throws RocksDBException {
        int port = 9981;
        int type = 5;
        try {
            port = Integer.parseInt(args[0]);
            type = Integer.parseInt(args[1]);
        } catch (Exception e) {
        }

        KVDataClient client = new KVDataClient();
        //client.handle("localhost", port, type);
        client.handleWithSocket("localhost", port, type);

    }
    public void handle(String ip, int port, int type) {
        long start = System.currentTimeMillis();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        ReadHandler handler = new ReadHandler();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();


                        pipeline.addLast(handler);
                        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        try {
            System.out.println(System.currentTimeMillis() - start);
            start = System.currentTimeMillis();
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            System.out.println(System.currentTimeMillis() - start);

            future.channel().writeAndFlush(type);
            cyclicBarrier.await();
            System.out.println(handler.getMsg());

            workerGroup.shutdownGracefully();
        } catch (InterruptedException e) {
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            //e.printStackTrace();
        }

    }

    void handleWithSocket(String ip, int port, int type) {
        try {
            Socket socket = new Socket(ip, port);
            //构建IO
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            os.write(type);
            os.flush();
            //读取服务器返回的消息
            int len = is.read();
            byte[] bytes = new byte[len];
            is.read(bytes);
            System.out.println(new String(bytes));
            socket.close();
            //zabbix_get -s '127.0.0.1' -p 10050 -k "kvstore.current_term"
        } catch (IOException e) {
            System.out.println(1);
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
