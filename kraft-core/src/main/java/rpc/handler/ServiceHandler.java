package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import rpc.message.AbstractRequest;


//处理入站事件
public class ServiceHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        AbstractRequest abstractRequest = (AbstractRequest) msg;
        Object requestBody = abstractRequest.getRequestBody();

        //消息的处理交由对应角色提供的回调函数来处理，拿到结果后向对端发送回复
        //异步处理
        //并发控制如何做？

        //ctx.writeAndFlush()



        super.channelRead(ctx, msg);
    }
}
