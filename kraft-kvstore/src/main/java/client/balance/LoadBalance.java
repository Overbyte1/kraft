package client.balance;

import rpc.NodeEndpoint;

public interface LoadBalance {
    //轮询、随机、最短响应时间优先、Leader优先
    Object send(Object msg);
    Object send(String ip, int port, Object msg);
    //void initServerList(NodeEndpoint[] nodeEndpoints);
    void initRouter(NodeEndpoint[] nodeEndpoints);
}
