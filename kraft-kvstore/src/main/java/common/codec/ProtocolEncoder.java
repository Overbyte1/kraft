package common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public class ProtocolEncoder extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        //TODO:protobuf序列化
        /*Json序列化
        String jsonStr = JSON.toJSONString(msg);
        byte[] bytes = jsonStr.getBytes();
         */


    }

}
