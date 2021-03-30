package server;

import common.message.Connection;
import common.message.DelCommand;
import common.message.GetCommand;
import common.message.SetCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServiceHandler extends ChannelInboundHandlerAdapter {
    private KVStore kvStore;

    public ServiceHandler(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if(msg instanceof GetCommand) {
            kvStore.handleGetCommand(new Connection<>((GetCommand)msg, channel));
        } else if(msg instanceof SetCommand) {
            kvStore.handleSetCommand(new Connection<>((SetCommand)msg, channel));
        } else if(msg instanceof DelCommand) {
            kvStore.handleDelCommand(new Connection<>((DelCommand)msg, channel));
        }

        super.channelRead(ctx, msg);
    }
}
