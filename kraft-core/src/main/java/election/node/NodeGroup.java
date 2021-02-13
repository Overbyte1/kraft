package election.node;

import java.util.Map;

public class NodeGroup {
    private Map<NodeId, GroupMember> nodesMap;

    public Map<NodeId, GroupMember> getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(Map<NodeId, GroupMember> nodesMap) {
        this.nodesMap = nodesMap;
    }
}
