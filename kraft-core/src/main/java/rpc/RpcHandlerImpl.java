package rpc;

import election.log.LogEntry;
import election.node.NodeId;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import rpc.codec.FrameDecoder;
import rpc.codec.FrameEncoder;
import rpc.codec.ProtocolDecoder;
import rpc.codec.ProtocolEncoder;
import rpc.handler.ServiceInboundHandler;

import java.util.List;

public class RpcHandlerImpl implements RpcHandler {
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    private int port;


    @Override
    public void initialize() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(NioChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new FrameDecoder());
                        pipeline.addLast(new FrameEncoder());
                        pipeline.addLast(new ProtocolDecoder());
                        pipeline.addLast(new ProtocolEncoder());

                        pipeline.addLast(new ServiceInboundHandler());
                    }
                })
    }

    @Override
    public void sendRequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm) {


    }

    @Override
    public void sendAppendEntriesMessage(long term, NodeId leaderId, long preLogIndex, long preLogTerm,
                                         List<LogEntry> logEntryList, long leaderCommit) {

    }

    @Override
    public void sendRequestVoteResultMessage(long term, boolean voteGranted) {

    }

    @Override
    public void sendAppendEntriesResultMessage(long term, boolean success) {

    }
}
