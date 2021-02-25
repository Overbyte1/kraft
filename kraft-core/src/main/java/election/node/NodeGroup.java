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

    public void resetReplicationState(long lastLogIndex) {
        for (GroupMember member : nodesMap.values()) {
            member.resetReplicationState(lastLogIndex);
        }
    }
    //判断matchIndex是否过半
    public boolean isMajorMatchIndex(long index) {
        ReplicationState replicationState = null;
        int majorNum = (nodesMap.size() + 1) / 2;
        for (GroupMember member : nodesMap.values()) {
            replicationState = member.getReplicationState();
            if(replicationState.getMatchIndex() >= index) {
                majorNum--;
            }
            if(majorNum == 0) {
                return true;
            }
        }
        return false;
    }
}
