package election.log;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.List;

public class DefaultLog {
    private long commitIndex;
    private long lastApplied;
    private LogStore logStore;
    private StateMachine stateMachine;
    private ReplicationState replicationState;

    public void advanceCommit() {

    }
    public boolean isNewerThan(long logIndex) {
        Entry logEntry = logStore.getLogEntry(logIndex);
        return logIndex > logEntry.getIndex();
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

    }

    public boolean appendEntries(long preTerm, long preLogIndex, long logIndex, List<Entry> entryList) {
        if(!isMatch(preTerm, preLogIndex, logIndex)) {
            return logStore.appendEntries(entryList);
        }
        return false;
    }
    public void decNextIndex(NodeId nodeId) {

    }
    public void incNextIndex(NodeId nodeId) {

    }
    public void incMatchIndex(NodeId nodeId) {

    }
    public AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term) {
        int nextIndex = replicationState.getNextIndex();
        EntryMeta entryMeta = logStore.getEntryMata(nextIndex);
        List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
        AppendEntriesMessage message = new AppendEntriesMessage(term, leaderId, entryMeta.getTerm(),
                                        entryMeta.getPreLogIndex(), entryList);
        return message;
    }
    public RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term) {
        //TODO:需要保证原子性？
        long lastLogIndex = logStore.getLastLogIndex();
        EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);

        return new RequestVoteMessage(term, candidateId, entryMata.getTerm(), entryMata.getPreLogIndex());
    }


}
