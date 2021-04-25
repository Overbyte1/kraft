package election.role;

import election.node.NodeId;


public class LeaderRole extends AbstractRole {
    public LeaderRole(long term) {
        super(term);
    }
    public LeaderRole(NodeId nodeId, long term) {
        this(nodeId, RoleType.LEADER, term, nodeId);
    }
    public LeaderRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
    }

    @Override
    public NodeId getLeaderId() {
        return getNodeId();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
