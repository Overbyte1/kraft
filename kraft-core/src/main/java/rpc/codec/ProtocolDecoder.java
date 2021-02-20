package rpc.codec;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class ProtocolDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //TODO:protobuf反序列化
        //先暂时使用Json序列化
        int byteNum = in.readableBytes();
        byte[] bytes = new byte[byteNum];
        in.readBytes(bytes);
        String jsonStr = new String(bytes);
        System.out.println(jsonStr);
        Object object = JSON.parse(jsonStr);
//        JsonObjectDecoder jsonObjectDecoder = new JsonObjectDecoder();
//
//        //jsonObjectDecoder
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
//        Object object = objectInputStream.readObject();
        out.add(object);
    }
}
