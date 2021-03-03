package election.role;

import election.node.NodeId;

public class FollowerRole extends AbstractRole {
    public FollowerRole(long term) {
        super(term);
    }
    public FollowerRole(NodeId nodeId, long currentTerm) {
        this(nodeId, RoleType.FOLLOWER, currentTerm, null);
    }
    public FollowerRole(NodeId nodeId, long currentTerm, NodeId voteFor) {
        this(nodeId, RoleType.FOLLOWER, currentTerm, voteFor);
    }
    public FollowerRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
