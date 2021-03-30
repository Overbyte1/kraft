package server;

import common.message.DelCommand;
import common.message.GetCommand;
import common.message.SetCommand;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServiceHandler extends ChannelInboundHandlerAdapter {
    private KVStore kvStore;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof GetCommand) {

        } else if(msg instanceof SetCommand) {

        } else if(msg instanceof DelCommand) {

        }

        super.channelRead(ctx, msg);
    }
}
