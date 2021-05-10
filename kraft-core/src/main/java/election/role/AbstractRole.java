package election.role;

import log.entry.GeneralEntry;
import election.node.NodeId;
import election.node.NodeIdGenerator;

import java.util.List;
import java.util.Objects;

public abstract class AbstractRole {
    //节点ID
    private NodeId nodeId;
    //节点名称
    private RoleType roleType;

    /*持久化数据*/
    //当前任期
    private long currentTerm;
    //投票给的那个节点
    private NodeId voteFor;

    /*易失性数据*/
    //已知已提交的最高的日志条目的索引（初始值为0，单调递增）
    //private long commitIndex;
    //已经被应用到状态机的最高的日志条目的索引（初始值为0，单调递增）
    //private long lastApplied;
    public AbstractRole(long currentTerm) {
        this(null, currentTerm);
    }

    public AbstractRole(RoleType roleType, long currentTerm) {
        this(roleType, currentTerm, null);
    }

    public AbstractRole(RoleType roleType, long currentTerm, NodeId voteFor) {
        this(NodeIdGenerator.generate(), roleType, currentTerm, voteFor);
    }

    public AbstractRole(NodeId nodeId, RoleType roleType, long currentTerm, NodeId voteFor) {
        this.nodeId = nodeId;
        this.roleType = roleType;
        this.currentTerm = currentTerm;
        this.voteFor = voteFor;
    }
    public abstract NodeId getLeaderId();


    public NodeId getNodeId() {
        return nodeId;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public NodeId getVoteFor() {
        return voteFor;
    }

    public void setVoteFor(NodeId voteFor) {
        this.voteFor = voteFor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRole role = (AbstractRole) o;
        return currentTerm == role.currentTerm &&
                Objects.equals(nodeId, role.nodeId) &&
                roleType == role.roleType &&
                Objects.equals(voteFor, role.voteFor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, roleType, currentTerm, voteFor);
    }
}
