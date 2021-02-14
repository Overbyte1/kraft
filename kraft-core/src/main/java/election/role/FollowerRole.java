package election.role;

import election.node.NodeId;

public class FollowerRole extends AbstractRole {
    public FollowerRole(long term) {
        super(term);
    }
    public FollowerRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
    }
}
