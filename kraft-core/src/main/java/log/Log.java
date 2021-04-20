package log;

import log.entry.EmptyEntry;
import log.entry.Entry;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.List;

public interface Log {
    /**
     * 增加 commitIndex
     * @return 成功返回 true，否则返回 false
     */
    boolean advanceCommitForLeader(long currentTerm);

    boolean updateReplicationState(ReplicationState state, long addNum);

    long getLastLogIndex();

    /**
     * 判断当前节点最后一条日志是否比给定的 lastTerm、lastLogIndex对应的日志 新，
     * 在当前节点的角色是Candidate并且收到请求投票消息时会被调用
     * @param lastTerm
     * @param lastLogIndex
     * @return 当前节点的日志更新则返回true，否则返回false
     */
    boolean isNewerThan(long lastTerm,  long lastLogIndex);

    /**
     * 创建常规的附加日志消息
     * @param leaderId
     * @param term
     * @param nextIndex
     * @return
     */
    AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term, long nextIndex);

    /**
     * 创建请求投票消息
     * @param candidateId
     * @param term
     * @return
     */
    RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term);

    /**
     * 附加空的日志（心跳消息）
     * @param term
     * @return
     */
    EmptyEntry appendEmptyEntry(long term);

    boolean appendGeneralEntry(long term, byte[] command);

    /**
     * 附加常规日志，也就是应用层的一些请求命令对应的日志
     * @param preTerm
     * @param preLogIndex
     * @param entryList
     * @return
     */
    boolean appendGeneralEntriesFromLeader(long preTerm, long preLogIndex, List<Entry> entryList, long leaderCommit);

    /**
     * 将命令应用到状态机
     */
    void apply(byte[] command);

    /**
     * 注册状态机
     * @param stateMachine
     */
    void registerStateMachine(StateMachine stateMachine);


}
