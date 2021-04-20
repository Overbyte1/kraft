package log;

import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.EntryMeta;
import log.entry.EntryType;
import log.store.LogStore;
import election.node.NodeGroup;
import election.node.NodeId;
import election.statemachine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.List;

public class DefaultLog {
    private static final Logger logger = LoggerFactory.getLogger(DefaultLog.class);

    private long commitIndex;
    private long lastApplied;
    private LogStore logStore;
    private StateMachine stateMachine;
    private NodeGroup nodeGroup;

    public DefaultLog(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.nodeGroup = nodeGroup;
        init();
    }
    private void init() {

    }

    public synchronized boolean advanceCommit(long currentTerm) {
        EntryMeta entryMata = logStore.getEntryMata(commitIndex);
        //TODO:+1幅度过小，而且每次都要遍历所有的节点。优化思路：维护最小的过半 matchIndex 的值
        if(currentTerm == entryMata.getTerm() && nodeGroup.isMajorMatchIndex(commitIndex + 1)) {
            commitIndex++;
            apply();
            return true;
        }
        return false;
    }

    /**
     * Raft 通过比较两份日志中最后一条日志条目的索引值和任期号定义谁的日志比较新：
     * 如果两份日志最后的条目的任期号不同，那么任期号大的日志更加新。
     * 如果两份日志最后的条目任期号相同，那么日志比较长的那个就更加新。
     * @param term
     * @param logIndex
     * @return
     */
    public boolean isNewerThan(long term,  long logIndex) {
        EntryMeta entryMata = logStore.getEntryMata(logIndex);
        if(entryMata.getTerm() != term) {
            return term > entryMata.getTerm();
        }
        return logIndex > entryMata.getLogIndex();
    }
    //获取最后的日志信息
    public Entry getLastEntry() {
        return logStore.getLastEntry();
    }

    //preTerm和preIndex是否匹配
    public boolean isMatch(long preLogTerm, long preLogIndex, long logIndex) {
        return false;
    }
    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
    public void apply() {
        logger.info("appendLog() was called");
    }

    public boolean appendEntries(long preTerm, long preLogIndex, long logIndex, List<Entry> entryList) {
        if(!isMatch(preTerm, preLogIndex, logIndex)) {
            return logStore.appendEntries(preTerm, preLogIndex, entryList);
        }
        return false;
    }
    public EmptyEntry appendEmptyEntry(long term) {
        //这是成为Leader后添加的第一条日志，其他上层应用的日志必须等待该条日志添加完毕才能添加，所以是线程安全的
        EmptyEntry entry = new EmptyEntry(EntryType.Empty, term);
        logStore.appendEmptyEntry(entry);
        return entry;
    }
    public void decNextIndex(NodeId nodeId) {

    }
    public void incNextIndex(NodeId nodeId) {

    }
    public void incMatchIndex(NodeId nodeId) {

    }
    public AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term, long nextIndex) {
        //long nextIndex = replicationState.getNextIndex();
        EntryMeta entryMeta = logStore.getEntryMata(nextIndex);
        List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
//        AppendEntriesMessage message = new AppendEntriesMessage(term, leaderId, entryMeta.getTerm(),
//                                        entryMeta.getLogIndex(), entryList);
        return null;
    }

    public RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term) {
        long lastLogIndex = logStore.getLastLogIndex();
        EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);

        return new RequestVoteMessage(term, candidateId, entryMata.getTerm(), entryMata.getLogIndex());
    }
    public long getLastLogIndex() {
        return logStore.getLastLogIndex();
    }


}
