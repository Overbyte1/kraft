package client;

import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SocketChannel {
    private String ip;
    private int port;
    private Channel channel;
    private Lock lock;
    private Condition condition;
    private ReadHandler readHandler;
    //TODO:配置
    private long timeout = 5000;

    public SocketChannel() {
        ip = null;
        port = 0;
        channel = null;
        lock = new ReentrantLock();
        condition = lock.newCondition();
        readHandler = new ReadHandler();
    }

    private Object send(String ip, int port, Object msg) throws SendTimeoutException {
        if(this.ip == null || this.port == 0 || channel == null) {
            connect(ip, port);
        }
        channel.writeAndFlush(msg);
        try {
            lock.lock();
            condition.await(timeout, TimeUnit.MILLISECONDS);
            Object message =  readHandler.getMessage();
            if(message != null) {
                return message;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        throw new SendTimeoutException("send timeout, send time exceed " + timeout + " milliseconds");
    }

    private void connect(String ip, int port) {
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(1);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder())
                                .addLast(new FrameEncoder())
                                .addLast(new ProtocolDecoder())
                                .addLast(new ProtocolEncoder())
                                .addLast(readHandler)
                                .addLast(new LoggingHandler(LogLevel.INFO));
                    }
                });
        ChannelFuture future = null;
        try {
            future = bootstrap.connect(ip, port).sync();
            channel = future.channel();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

    }
    private class ReadHandler extends ChannelInboundHandlerAdapter {
        private Object message;
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                lock.lock();
                message = msg;
                condition.signal();
            } finally {
                lock.unlock();
            }
            super.channelRead(ctx, msg);
        }

        public Object getMessage() {
            Object ret =  message;
            message = null;
            return ret;
        }
    }
}
