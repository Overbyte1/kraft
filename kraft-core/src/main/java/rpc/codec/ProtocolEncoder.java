package rpc.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import rpc.message.AbstractMessage;

public class ProtocolEncoder extends MessageToByteEncoder<AbstractMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractMessage msg, ByteBuf out) throws Exception {
        //TODO:protobuf序列化
        //先暂时使用Json序列化
        String jsonStr = JSON.toJSONString(msg);
        byte[] bytes = jsonStr.getBytes();

        out.writeBytes(bytes);

    }

}
