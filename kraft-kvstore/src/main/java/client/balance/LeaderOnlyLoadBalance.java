package client.balance;

import rpc.NodeEndpoint;

public class LeaderOnlyLoadBalance implements LoadBalance {
    @Override
    public Object send(Object msg) {
        return null;
    }

    @Override
    public Object send(String ip, int port, Object msg) {
        return null;
    }

    @Override
    public void initRouter(NodeEndpoint[] nodeEndpoints) {

    }
}
