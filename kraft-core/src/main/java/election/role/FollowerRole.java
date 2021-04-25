package election.role;

import election.node.NodeId;

public class FollowerRole extends AbstractRole {
    private NodeId leaderId;

    public FollowerRole(long term, NodeId leaderId) {
        super(term);
        this.leaderId = leaderId;
    }
    public FollowerRole(NodeId nodeId, long currentTerm) {
        this(nodeId, RoleType.FOLLOWER, currentTerm, null, null);
    }
    public FollowerRole(NodeId nodeId, long currentTerm, NodeId leaderId) {
        this(nodeId, RoleType.FOLLOWER, currentTerm, leaderId, null);
    }
    public FollowerRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId leaderId, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
        this.leaderId = leaderId;
    }

    @Override
    public NodeId getLeaderId() {
        return leaderId;
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
