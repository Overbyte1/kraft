package election.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.NodeEndpoint;

public class GroupMember {
    private static final Logger logger = LoggerFactory.getLogger(GroupMember.class);
    private volatile ReplicationState replicationState;
    private NodeEndpoint nodeEndpoint;

    public GroupMember(ReplicationState replicationState, NodeEndpoint nodeEndpoint) {
        this.replicationState = replicationState;
        this.nodeEndpoint = nodeEndpoint;
    }
    public GroupMember(NodeEndpoint nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public NodeEndpoint getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(NodeEndpoint nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public ReplicationState getReplicationState() {
        return replicationState;
    }

    public void setReplicationState(ReplicationState replicationState) {
        this.replicationState = replicationState;
    }

    public void resetReplicationState(long lastLogIndex) {
        replicationState.setMatchIndex(0);
        replicationState.setNextIndex(lastLogIndex);
    }

    public boolean isReplicating() {
        if(replicationState == null) {
            replicationState = new ReplicationState(0, 0);
        }
        return replicationState.isReplicating();
    }

    public void startReplication() {
        if(replicationState.isReplicating()) {
            logger.info("the member {} has started replicating log, no need to start again", nodeEndpoint);
        } else {
            replicationState.startReplication();
        }
    }

    public void stopReplication() {
        if(!replicationState.isReplicating()) {
            logger.info("the member {} has stop replicating log. no need to stop again", nodeEndpoint);
        } else {
            replicationState.stopReplication();
        }
    }

    /**
     * 判断该节点是否可以进行日志复制
     * @param interval
     * @return 当该节点未停止日志复制并且距离上一次日志复制时间已经超过 interval时返回true，否则返回false
     */
    public boolean shouldReplication(long interval) {
        if(!isReplicating()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        long lastReplicationTime = replicationState.getLastReplicationTime();
        return currentTime - lastReplicationTime >= interval;
    }
    public void updateReplicationTime() {
        replicationState.setLastReplicationTime(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "GroupMember{" +
                "replicationState=" + replicationState +
                ", nodeEndpoint=" + nodeEndpoint +
                '}';
    }
}
