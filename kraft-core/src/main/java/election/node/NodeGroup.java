package election.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeGroup {
    private static final Logger logger = LoggerFactory.getLogger(NodeGroup.class);
    private Map<NodeId, GroupMember> nodesMap = new HashMap<>();
    private NodeId selfNodeId;

    public void setSelfNodeId(NodeId selfNodeId) {
        this.selfNodeId = selfNodeId;
    }

    public NodeId getSelfNodeId() {
        return selfNodeId;
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
        System.out.println("check major index: " + index);
        ReplicationState replicationState;
        //nodesMap保存了其他节点的复制状态，除去当前Leader节点，只需要nodesMap.size()/2的节点符合条件matchIndex >= index即可
        int majorNum = nodesMap.size() / 2;
        for (GroupMember member : nodesMap.values()) {
            if(member.getNodeEndpoint().getNodeId().equals(selfNodeId)) {
                continue;
            }
            replicationState = member.getReplicationState();
            System.out.println(replicationState);
            if(replicationState.getMatchIndex() >= index) {
                majorNum--;
            }
            if(majorNum == 0) {
                return true;
            }
        }
        return false;
    }
    public long getMajorMatchIndex() {
        long[] arr = new long[nodesMap.size()];
        int idx = 0;
        for (GroupMember member : nodesMap.values()) {
            if(member.getNodeEndpoint().getNodeId().equals(selfNodeId)) {
                arr[idx] = Long.MAX_VALUE;
            } else {
                arr[idx] = member.getReplicationState().getMatchIndex();
            }
            idx++;
        }
        Arrays.sort(arr);
        long ret = arr.length % 2 == 0 ? arr[arr.length / 2 - 1] : arr[arr.length / 2];
        logger.debug("major match index is: {}", ret);
        return ret;
    }
}
