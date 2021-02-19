package rpc;

import election.node.NodeId;

import java.util.concurrent.ConcurrentMap;

/**
 * 管理出站连接
 */
public class OutboundChannelGroup {
    private NodeId selfId;
    private ConcurrentMap<NodeId, NioChannel> channelConcurrentMap;

}
