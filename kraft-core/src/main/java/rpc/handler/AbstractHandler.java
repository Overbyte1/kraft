package rpc.handler;

import election.handler.MessageHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AbstractMessage;

import java.util.Map;

public class AbstractHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);
//    private Handler handler;
//    private Class<?> type;
    private Map<Class, MessageHandler> handlerMap;
    private void addHandler(Class type, MessageHandler handler) {
        handlerMap.put(type, handler);
    }
    private void removeHandler(Class type) {
        handlerMap.remove(type);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MessageHandler handler = handlerMap.get(msg.getClass());
        if(handler == null) {
            logger.warn("Handler not found, msg = {}", msg);
            super.channelRead(ctx, msg);
            return;
        }
        AbstractMessage message = (AbstractMessage)msg;
        handler.handle(message.getBody());

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn(cause.toString());
        ctx.close();
    }
}
