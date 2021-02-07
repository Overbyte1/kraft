package rpc.handler;

import io.netty.handler.timeout.IdleStateHandler;

//TODO:心跳包处理
public class KeepAliveHandler extends IdleStateHandler {
    public KeepAliveHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }
}
