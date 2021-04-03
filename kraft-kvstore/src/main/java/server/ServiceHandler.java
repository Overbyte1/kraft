package server;

import common.message.*;
import common.message.command.DelCommand;
import common.message.command.GetCommand;
import common.message.command.SetCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServiceHandler.class);
    private KVDatabase kvDatabase;

    public ServiceHandler(KVDatabase kvDatabase) {
        this.kvDatabase = kvDatabase;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.debug("receive message: {}", msg);
        Channel channel = ctx.channel();
        if(msg instanceof GetCommand) {
            kvDatabase.handleGetCommand(new Connection<>((GetCommand)msg, channel));
        } else if(msg instanceof SetCommand) {
            kvDatabase.handleSetCommand(new Connection<>((SetCommand)msg, channel));
        } else if(msg instanceof DelCommand) {
            kvDatabase.handleDelCommand(new Connection<>((DelCommand)msg, channel));
        } else {
            channel.writeAndFlush(Failure.NOT_SUPPORT_OPERATION);
        }

        super.channelRead(ctx, msg);
    }
}
