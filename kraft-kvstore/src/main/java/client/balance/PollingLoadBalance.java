package client.balance;

import client.SendTimeoutException;
import client.SocketChannelImpl;
import election.node.NodeId;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 负载均衡策略：轮询
 */
public class PollingLoadBalance extends AbstractLoadBalance {
    private List<NodeEndpoint> inlineServerList;
    private List<NodeEndpoint> timeoutServerList;
    private Iterator<NodeEndpoint> iterator;

    public PollingLoadBalance(Map<NodeId, Endpoint> endpointMap) {
        super(new SocketChannelImpl());

        inlineServerList = new LinkedList<>();
        for (Map.Entry<NodeId, Endpoint> entry : endpointMap.entrySet()) {
            inlineServerList.add(new NodeEndpoint(entry.getKey(), entry.getValue()));
        }
        iterator = inlineServerList.listIterator();
    }

    @Override
    public Object send(Object msg) {
        Object resp;
        while (inlineServerList.size() > 0) {
            if(!iterator.hasNext()) {
                iterator = inlineServerList.listIterator();
            }
            NodeEndpoint nodeEndpoint = iterator.next();
            try {
                resp = doSend(nodeEndpoint.getEndpoint(), msg);
                return resp;
            } catch (SendTimeoutException e) {
                getTimeoutServerList().add(nodeEndpoint);
            }
        }
        throw new NoAvailableServerException("no available server");
    }
    private List<NodeEndpoint> getTimeoutServerList() {
        if(timeoutServerList == null) {
            timeoutServerList = new LinkedList<>();
        }
        return timeoutServerList;
    }

}
