package election.log;

import election.log.entry.*;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.List;

public class LogImpl implements Log {
    private LogStore logStore;
    private StateMachine stateMachine;
    private long commitIndex;
    private long appiedIndex;
    private NodeGroup nodeGroup;

    public LogImpl(LogStore logStore, StateMachine stateMachine, long commitIndex, NodeGroup nodeGroup) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.commitIndex = commitIndex;
        this.nodeGroup = nodeGroup;
    }

    /**
     * 增加commitIndex，只会被Leader调用
     * @param currentTerm
     * @param n 增加的值，n > 0
     * @return
     */
    @Override
    public boolean advanceCommit(long currentTerm, long n) {
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
    public AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term, long nextIndex) {
        EntryMeta entryMeta = logStore.getEntryMata(nextIndex);
        List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
        AppendEntriesMessage message = new AppendEntriesMessage(term, leaderId, entryMeta.getTerm(),
                entryMeta.getLogIndex(), commitIndex, entryList);
        return message;
    }

    @Override
    public RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term) {
        //TODO:需要保证原子性？
        long lastLogIndex = logStore.getLastLogIndex();
        EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);

        return new RequestVoteMessage(term, candidateId, entryMata.getTerm(), entryMata.getLogIndex());
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
                                                  ReplicationState state, long leaderCommit) {
        boolean result = logStore.appendEntries(preTerm, preLogIndex, entryList);
        if(result) {
            //更新commitIndex、appliedIndex
            commitIndex = Math.min(leaderCommit, logStore.getLastLogIndex());
            if(appiedIndex < commitIndex) {
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
