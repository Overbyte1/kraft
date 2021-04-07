package common.message.response;

import rpc.NodeEndpoint;

import java.io.Serializable;

public class RedirectResult implements Serializable {
    private NodeEndpoint nodeEndpoint;

    public RedirectResult(NodeEndpoint nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public NodeEndpoint getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(NodeEndpoint nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RedirectResult that = (RedirectResult) o;

        return nodeEndpoint != null ? nodeEndpoint.equals(that.nodeEndpoint) : that.nodeEndpoint == null;
    }

    @Override
    public int hashCode() {
        return nodeEndpoint != null ? nodeEndpoint.hashCode() : 0;
    }
}
