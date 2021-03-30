package common.message;

import rpc.NodeEndpoint;

public class RedirectResult {
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
}
