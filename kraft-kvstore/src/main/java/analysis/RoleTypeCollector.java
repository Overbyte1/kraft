package analysis;

import election.node.Node;
import election.role.RoleType;

public class RoleTypeCollector implements Collector {
    private final Node node;

    public RoleTypeCollector(Node node) {
        this.node = node;
    }

    @Override
    public int getType() {
        return AnalysisType.ROLE_TYPE;
    }

    @Override
    public String collect() {
        RoleType roleType = node.getRoleType();
        if(roleType == RoleType.FOLLOWER) {
            return "Follower";
        }
        if(roleType == RoleType.CANDIDATE) {
            return "Candidate";
        }
        if(roleType == RoleType.LEADER) {
            return "Leader";
        }
        return "Unknown role type";
    }
}
