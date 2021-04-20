package rpc.handler;

import io.netty.handler.timeout.IdleStateHandler;

public class KeepAliveHandler extends IdleStateHandler {
    public KeepAliveHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }
}
