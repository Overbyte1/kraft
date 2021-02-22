package rpc;

import election.node.NodeId;

public class NodeEndpoint {
    private NodeId nodeId;
    private Endpoint endpoint;

    public NodeEndpoint(NodeId nodeId, Endpoint endpoint) {
        this.nodeId = nodeId;
        this.endpoint = endpoint;
    }

    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String toString() {
        return "NodeEndpoint{" +
                "nodeId=" + nodeId +
                ", endpoint=" + endpoint +
                '}';
    }
}
