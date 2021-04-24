package client.balance;

import client.Router;
import client.SendTimeoutException;
import client.SocketChannelImpl;
import client.config.ClientConfig;
import election.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.*;

/**
 * 负载均衡策略：轮询
 */
public class PollingLoadBalance extends AbstractLoadBalance {
    private static final Logger logger = LoggerFactory.getLogger(PollingLoadBalance.class);
//    private List<NodeEndpoint> inlineServerList;
//    private List<NodeEndpoint> timeoutServerList;
    private final Set<Endpoint> inlineSet;
    private final Set<Endpoint> outlineSet;
    private Iterator<Endpoint> iterator;

    public PollingLoadBalance(ClientConfig config, Router router) {
        super(new SocketChannelImpl(config.getConnectTimeout()), router);
//        inlineServerList = new LinkedList<>();
        Map<NodeId, Endpoint> endpointMap = router.getNodeEndpointMap();
        inlineSet = new HashSet<>();
        for (Map.Entry<NodeId, Endpoint> entry : endpointMap.entrySet()) {
            inlineSet.add(entry.getValue());
        }
        outlineSet = new HashSet<>();
        iterator = inlineSet.iterator();

    }

    @Override
    public Object send(Object msg) {
        Object resp;
        while (inlineSet.size() > 0) {
            if(!iterator.hasNext()) {
                iterator = inlineSet.iterator();
            }
            Endpoint endpoint = iterator.next();
            try {
                logger.info("send request to: {}", endpoint);
                resp = doSend(endpoint, msg);
                return resp;
            } catch (SendTimeoutException e) {
                outlineSet.add(endpoint);
                iterator.remove();
            }
        }
        throw new NoAvailableServerException("no available server");
    }

    @Override
    public Object send(String ip, int port, Object msg) {
        try {
            Endpoint endpoint = new Endpoint(ip, port);
            Object resp = doSend(endpoint, msg);

            if(outlineSet.contains(endpoint)) {
                outlineSet.remove(endpoint);
                inlineSet.add(endpoint);
                iterator = inlineSet.iterator();
            }
            return resp;
        } catch (SendTimeoutException e) {
            logger.info("send timeout: endpoint[{}:{}], message: {}", ip, port, msg);
            throw e;
        }
    }

    @Override
    public void initRouter(NodeEndpoint[] nodeEndpoints) {
        router.resetMap(nodeEndpoints);
    }

}
