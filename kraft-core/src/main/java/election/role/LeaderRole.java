package election.role;

import election.NodeId;

import java.util.Map;

public class LeaderRole extends AbstractRole {
    private Map<NodeId, Long> nextIndexMap;
    private Map<NodeId, Long> matchIndexMap;


}
