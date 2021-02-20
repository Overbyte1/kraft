package rpc.handler;

import election.handler.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AbstractMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据消息类型注册对应消息的处理器后，当消息到来时，会调用其注册的处理器
 */
public class ServiceInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServiceInboundHandler.class);

    private Map<Class, MessageHandler> handlerMap;

    public ServiceInboundHandler() {
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

        MessageHandler handler = handlerMap.get(msg.getClass());
        if (handler == null) {
            logger.warn("Handler not found, msg = {}", msg);
            super.channelRead(ctx, msg);
            return;
        }
        //处理
        handler.handle(msg);

        super.channelRead(ctx, msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn(cause.toString());
        ctx.close();
    }
}
