package client.balance;

import client.Router;
import client.SocketChannel;
import client.SocketChannelImpl;
import common.message.response.RedirectResult;
import common.message.response.Response;
import common.message.response.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.Endpoint;
import rpc.NodeEndpoint;

public abstract class AbstractLoadBalance implements LoadBalance {
    private static final Logger logger = LoggerFactory.getLogger(AbstractLoadBalance.class);
    private SocketChannel channel;
    protected Router router;

    public AbstractLoadBalance(SocketChannel channel, Router router) {
        this.channel = channel;
        this.router = router;
    }

    public Object doSend(Endpoint endpoint, Object msg) {
        Object resp =  channel.send(endpoint, msg);
        if(resp instanceof Response && ((Response)resp).getType() == ResponseType.REDIRECT) {
            Endpoint redirectEndpoint = ((Response<RedirectResult>)resp).getBody().getNodeEndpoint().getEndpoint();
            logger.info("redirect to [{}:{}]" , endpoint.getIpAddress(), endpoint.getPort());
            return channel.send(redirectEndpoint, msg);
        }
        return resp;
    }
}
