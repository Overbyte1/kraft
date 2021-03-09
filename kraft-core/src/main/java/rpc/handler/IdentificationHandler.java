package rpc.handler;

import election.node.NodeId;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.ChannelGroup;
import rpc.NioChannel;

public class IdentificationHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(IdentificationHandler.class);
    private ChannelGroup channelGroup;

    public IdentificationHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof NodeId) {
            NodeId nodeId = (NodeId) msg;
            channelGroup.addChannel(nodeId, new NioChannel(ctx.channel()));
            //不需要后面的handler进行处理
            ctx.fireChannelReadComplete();
            return;
        }
        super.channelRead(ctx, msg);
    }
}
