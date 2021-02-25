package election.node;

import rpc.NodeEndpoint;

public class GroupMember {
    private ReplicationState replicationState;
    private NodeEndpoint nodeEndpoint;

    public GroupMember(ReplicationState replicationState, NodeEndpoint nodeEndpoint) {
        this.replicationState = replicationState;
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
}
