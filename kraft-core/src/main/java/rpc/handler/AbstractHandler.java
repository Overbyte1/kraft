package rpc.handler;

import election.handler.MessageHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AbstractMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    private Map<Class, MessageHandler> handlerMap;

    public AbstractHandler() {
        handlerMap = new ConcurrentHashMap<>();
    }

    public void registerHandler(Class type, MessageHandler handler) {
        handlerMap.put(type, handler);
    }

    public void unregisterHandler(Class type) {
        handlerMap.remove(type);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AbstractMessage abstractMessage = (AbstractMessage) msg;
        Object messageBody = abstractMessage.getBody();

        MessageHandler handler = handlerMap.get(messageBody.getClass());
        if (handler == null) {
            logger.warn("Handler not found, msg = {}", msg);
            super.channelRead(ctx, msg);
            return;
        }
        //AbstractMessage message = (AbstractMessage)msg;
        //处理
        handler.handle(messageBody);

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn(cause.toString());
        ctx.close();
    }
}
