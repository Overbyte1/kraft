package election.role;

import election.node.NodeId;
import election.log.entry.LogEntry;
import election.node.NodeIdGenerator;

import java.util.List;

public abstract class AbstractRole {
    //节点ID
    private NodeId nodeId;
    //节点名称
    //TODO:移除该属性
    private RoleType roleType;

    /*持久化数据*/
    //当前任期
    private long currentTerm;
    //投票给的那个节点
    private NodeId voteFor;
    //所有日志，TODO：考虑持久化
    private List<LogEntry> logEntryList;

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

        //TODO：logEntryList初始化
    }


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

    public List<LogEntry> getLogEntryList() {
        return logEntryList;
    }

    public void setLogEntryList(List<LogEntry> logEntryList) {
        this.logEntryList = logEntryList;
    }

}
