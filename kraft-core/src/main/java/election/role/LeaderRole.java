package election.role;

import election.node.NodeId;


public class LeaderRole extends AbstractRole {
    public LeaderRole(long term) {
        super(term);
    }
    public LeaderRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
    }
}
