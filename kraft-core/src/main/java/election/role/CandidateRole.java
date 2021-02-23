package election.role;

import election.node.NodeId;

public class CandidateRole extends AbstractRole {
    //当前已经获取的票数
    private int voteCount = 1;
    //TODO：总票数如何统计？需要考虑节点的动态变更
    public CandidateRole(long currentTerm) {
        super(currentTerm);
    }
    public CandidateRole(NodeId nodeId, long currentTerm) {
        this(nodeId, currentTerm, null);
    }
    public CandidateRole(NodeId nodeId, long currentTerm, NodeId voteFor) {
        this(nodeId, RoleType.CANDIDATE, currentTerm, voteFor);
    }

    public CandidateRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        super(nodeId, roleType, currentTerm, voteFor);
    }
    public synchronized void incVoteCount() {
        voteCount++;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }
}
