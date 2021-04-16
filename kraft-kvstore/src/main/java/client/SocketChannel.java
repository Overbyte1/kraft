package client;

import rpc.Endpoint;

public interface SocketChannel {
    Object send(Endpoint endpoint, Object msg);
}
