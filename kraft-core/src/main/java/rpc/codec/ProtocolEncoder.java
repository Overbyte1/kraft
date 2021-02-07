package rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import rpc.message.AbstractResponse;

import java.util.List;

public class ProtocolEncoder extends MessageToByteEncoder<AbstractResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractResponse msg, ByteBuf out) throws Exception {
        //protobuf序列化
    }
}
