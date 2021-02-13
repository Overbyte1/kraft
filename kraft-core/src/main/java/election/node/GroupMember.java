package election.node;

import rpc.Endpoint;

public class GroupMember {
    private Endpoint endpoint;
    private ReplicationState replicationState;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public ReplicationState getReplicationState() {
        return replicationState;
    }

    public void setReplicationState(ReplicationState replicationState) {
        this.replicationState = replicationState;
    }
}
