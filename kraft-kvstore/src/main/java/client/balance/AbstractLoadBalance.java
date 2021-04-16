package client.balance;

import client.SocketChannel;
import rpc.Endpoint;

public abstract class AbstractLoadBalance implements LoadBalance {
    private SocketChannel channel;

    public AbstractLoadBalance(SocketChannel channel) {
        this.channel = channel;
    }

    public Object doSend(Endpoint endpoint, Object msg) {
        return channel.send(endpoint, msg);
    }
}
