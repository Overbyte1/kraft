package election.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeGroup {
    //TODO:添加自身节点
    private Map<NodeId, GroupMember> nodesMap = new HashMap<>();

    public Map<NodeId, GroupMember> getNodesMap() {
        return nodesMap;
    }

    public int getSize() {
        return nodesMap.size();
    }
    public GroupMember getGroupMember(NodeId nodeId) {
        return nodesMap.get(nodeId);
    }
    public void addGroupMember(NodeId nodeId, GroupMember groupMember) {
        nodesMap.put(nodeId, groupMember);
    }
    public Collection<GroupMember> getAllGroupMember() {
        return nodesMap.values();
    }
}
