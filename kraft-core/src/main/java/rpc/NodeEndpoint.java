package rpc;

import election.node.NodeId;

import java.io.Serializable;

public class NodeEndpoint implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeEndpoint that = (NodeEndpoint) o;

        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;
        return endpoint != null ? endpoint.equals(that.endpoint) : that.endpoint == null;
    }

    @Override
    public int hashCode() {
        int result = nodeId != null ? nodeId.hashCode() : 0;
        result = 31 * result + (endpoint != null ? endpoint.hashCode() : 0);
        return result;
    }
}
