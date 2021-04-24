package client;

import election.node.NodeId;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Router {
    private Map<NodeId, Endpoint> nodeEndpointMap;
    private NodeId leaderId;

    public Router(Map<NodeId, Endpoint> endpointMap) {
        nodeEndpointMap = new HashMap<>();
        for (Map.Entry<NodeId, Endpoint> entry : endpointMap.entrySet()) {
            nodeEndpointMap.put(entry.getKey(), entry.getValue());
        }
    }
    public boolean hasLeader() {
        return leaderId != null;
    }
    public Endpoint getServer(NodeId nodeId) {
        return nodeEndpointMap.get(nodeId);
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }
    public List<NodeEndpoint> getServerList() {
        List<NodeEndpoint> nodeEndpointList = new ArrayList<>();
        for (Map.Entry<NodeId, Endpoint> entry : nodeEndpointMap.entrySet()) {
            nodeEndpointList.add(new NodeEndpoint(entry.getKey(), entry.getValue()));
        }
        return nodeEndpointList;
    }

    public void resetMap(NodeEndpoint[] nodeEndpoints) {
        nodeEndpointMap.clear();
        for (NodeEndpoint nodeEndpoint : nodeEndpoints) {
            nodeEndpointMap.put(nodeEndpoint.getNodeId(), nodeEndpoint.getEndpoint());
        }
    }

    public Map<NodeId, Endpoint> getNodeEndpointMap() {
        return nodeEndpointMap;
    }
}
