package server;

import common.message.Connection;
import common.message.response.FailureResult;
import common.message.response.Response;
import common.message.response.ResponseType;
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
        try {
            kvDatabase.handleCommand(new Connection(msg, channel));
        } catch (Exception exception) {
            logger.warn("server can not execute this command: {}", msg);
            channel.writeAndFlush(new Response<FailureResult>(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR));
        }

        super.channelRead(ctx, msg);
    }
}
