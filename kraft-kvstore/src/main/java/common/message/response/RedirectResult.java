package common.message.response;

import rpc.NodeEndpoint;

import java.io.Serializable;
import java.util.Objects;

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
        return Objects.equals(nodeEndpoint, that.nodeEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeEndpoint);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RedirectResult{");
        sb.append("nodeEndpoint=").append(nodeEndpoint);
        sb.append('}');
        return sb.toString();
    }
}
