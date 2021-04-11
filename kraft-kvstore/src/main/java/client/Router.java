package client;

import election.node.NodeId;
import rpc.NodeEndpoint;

import java.util.Map;

public class Router {
    private Map<NodeId, NodeEndpoint> nodeEndpointMap;
    private NodeId leaderId;

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

}
