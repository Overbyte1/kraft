package election.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeGroup {
    private Map<NodeId, GroupMember> nodesMap = new HashMap<>();
    private NodeId selfNodeId;

    public void setSelfNodeId(NodeId selfNodeId) {
        this.selfNodeId = selfNodeId;
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
    /**
     * 判断索引为index的日志是否已经被过半复制
     */
    public boolean isMajorMatchIndex(long index) {
        ReplicationState replicationState = null;
        //nodesMap保存了其他节点的复制状态，除去当前Leader节点，只需要nodesMap.size()/2的节点符合条件matchIndex >= index即可
        int majorNum = nodesMap.size() / 2;
        for (GroupMember member : nodesMap.values()) {
            if(member.getNodeEndpoint().getNodeId().equals(selfNodeId)) {
                continue;
            }
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
