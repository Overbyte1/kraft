package client.balance;

import rpc.Endpoint;

public interface LoadBalance {
    //轮询、随机、最短响应时间优先、Leader优先
    Object send(Object msg);
}
