package client.balance;

import client.Router;
import client.SocketChannel;
import client.SocketChannelImpl;
import rpc.Endpoint;

public abstract class AbstractLoadBalance implements LoadBalance {
    private SocketChannel channel;
    protected Router router;

    public AbstractLoadBalance(SocketChannel channel, Router router) {
        this.channel = channel;
        this.router = router;
    }

    public Object doSend(Endpoint endpoint, Object msg) {
        return channel.send(endpoint, msg);
    }
    public Object doSend(String ip, int port, Object msg) {
        return channel.send(new Endpoint(ip, port), msg);
    }
}
