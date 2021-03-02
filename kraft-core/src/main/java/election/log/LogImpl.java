package election.log;

import election.log.entry.*;
import election.node.GroupMember;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.ArrayList;
import java.util.List;

public class LogImpl implements Log {
    private static final Logger logger = LoggerFactory.getLogger(LogImpl.class);
    private LogStore logStore;
    private StateMachine stateMachine;
    private long commitIndex;
    private long appliedIndex;
    private NodeGroup nodeGroup;

    public LogImpl(LogStore logStore, StateMachine stateMachine, long commitIndex, NodeGroup nodeGroup) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.commitIndex = commitIndex;
        this.nodeGroup = nodeGroup;
    }

    /**
     * 增加commitIndex，只会被Leader调用,
     * TODO：commitIndex推进需要过半matchIndex以及term，只有日志条目的term和自己的term一致才能更新commitIndex
     *
     * 如果存在一个满足 N > commitIndex的 N，并且大多数的 matchIndex[i] ≥ N成立，
     * 并且 log[N].term == currentTerm 成立，那么令 commitIndex 等于这个 N
     *
     * @param currentTerm
     * @param n 增加的值，n > 0
     * @return
     */
    @Override
    public boolean advanceCommitForLeader(long currentTerm, long n) {
        EntryMeta entryMata = logStore.getEntryMata(commitIndex);
        //TODO:+1幅度过小，而且每次都要遍历所有的节点。优化思路：维护最小的过半 matchIndex 的值
        if(currentTerm == entryMata.getTerm() && nodeGroup.isMajorMatchIndex(commitIndex + 1)) {
            long idx = commitIndex;
            commitIndex++;
            //apply(logStore.getLogEntry(idx));
            return true;
        }
        return false;
    }

    @Override
    public boolean updateReplicationState(ReplicationState replicationState) {
        //TODO:需要确定replicationState matchIndex与nextIndex增加的幅度
        replicationState.incMatchIndex();
        long lastLogIndex = logStore.getLastLogIndex();
        if(lastLogIndex > replicationState.getNextIndex()) {
            replicationState.incNextIndex();
        }
        return false;
    }

    @Override
    public long getLastLogIndex() {
        return logStore.getLastLogIndex();
    }

    /**
     * Raft 通过比较两份日志中最后一条日志条目的索引值和任期号定义谁的日志比较新：
     * 1. 如果两份日志最后的条目的任期号不同，那么任期号大的日志更加新。
     * 2. 如果两份日志最后的条目任期号相同，那么日志比较长的那个就更加新。
     * @param lastTerm
     * @param lastLogIndex
     * @return
     */
    @Override
    public boolean isNewerThan(long lastTerm, long lastLogIndex) {
        EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);
        if(entryMata.getTerm() != lastTerm) {
            return lastTerm > entryMata.getTerm();
        }
        return lastLogIndex > entryMata.getLogIndex();
    }

    @Override
    public AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term, GroupMember member) {
        ReplicationState state = member.getReplicationState();
        if(state == null) {
            state = new ReplicationState(0, logStore.getLastLogIndex());
            member.setReplicationState(state);
        }
        AppendEntriesMessage message = null;
        long nextIndex = state.getNextIndex();
        if(logStore.isEmpty()) {
            logger.warn("log store is empty, it have at least an empty log normally");
            message = new AppendEntriesMessage(term, leaderId, 0, 0, commitIndex, new ArrayList<>());
        } else {
            EntryMeta entryMeta = logStore.getEntryMata(nextIndex);
            List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
            message = new AppendEntriesMessage(term, leaderId, entryMeta.getTerm(),
                    entryMeta.getLogIndex(), commitIndex, entryList);
        }
        return message;
    }

    @Override
    public RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term) {
        //TODO:需要保证原子性？
        long lastLogIndex = logStore.getLastLogIndex();
        long lastLogTerm = 0;
        if(lastLogIndex > 0) {
            EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);
            lastLogTerm = entryMata.getTerm();
        }

        return new RequestVoteMessage(term, candidateId, lastLogTerm, lastLogIndex);
    }

    @Override
    public EmptyEntry appendEmptyEntry(long term) {
        //TODO:保证线程安全，有必要在Entry中维护index？
        EmptyEntry entry = new EmptyEntry(term, logStore.getLastLogIndex());
        logStore.appendEntry(entry);
        return entry;
    }

    /**
     * Leader附加一条常规日志，只会被Leader调用
     * @param term
     * @param command
     * @return
     */
    @Override
    public boolean appendGeneralEntry(long term, byte[] command) {
        GeneralEntry entry = new GeneralEntry(term, command);
        logStore.appendEntry(entry);
        return true;
    }

    @Override
    public boolean appendGeneralEntriesFromLeader(long preTerm, long preLogIndex, List<Entry> entryList,
                                                  long leaderCommit) {
        boolean result = logStore.appendEntries(preTerm, preLogIndex, entryList);
        if(result) {
            //更新commitIndex、appliedIndex
            commitIndex = Math.min(leaderCommit, logStore.getLastLogIndex());
            if(appliedIndex < commitIndex) {
                //TODO：异步执行命令
                for (Entry entry : entryList) {
                    apply(((GeneralEntry)entry).getCommandBytes());
                }
            }
        }
        return result;
    }


    @Override
    public void apply(byte[] command) {

    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
}
