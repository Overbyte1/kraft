package common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import utils.SerializationUtil;

import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //TODO:protobuf反序列化
        /*Json序列化
        int byteNum = in.readableBytes();
        byte[] bytes = new byte[byteNum];
        in.readBytes(bytes);
        String jsonStr = new String(bytes);
        System.out.println(jsonStr);
        Object object = JSON.parseObject(jsonStr, Object.class);

         */
        //JDK序列化
        //byte[] bytes = in.array();

        int byteNum = in.readableBytes();
        byte[] bytes = new byte[byteNum];
        in.readBytes(bytes);
        Object object = SerializationUtil.decode(bytes);
        out.add(object);

    }
}
