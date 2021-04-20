package rpc;

import election.node.NodeId;

import java.io.Serializable;
import java.util.Objects;

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
        return Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, endpoint);
    }

}
