package rpc.message;

import election.log.entry.Entry;
import election.log.entry.LogEntry;
import election.node.NodeId;

import java.io.Serializable;
import java.util.List;

public class AppendEntriesMessage implements Serializable {
    //Leader的term
    private long term;
    private NodeId leaderId;
    //Leader的上一个日志所属的term
    private long preLogTerm;
    //Leader的上一个日志的index
    private long preLogIndex;
    //发送的日志，为了提高效率可能一次性发送多个
    private List<Entry> entryList;

    public AppendEntriesMessage(long term, NodeId leaderId, long preLogTerm, long preLogIndex, List<Entry> entryList) {
        this.term = term;
        this.leaderId = leaderId;
        this.preLogTerm = preLogTerm;
        this.preLogIndex = preLogIndex;
        this.entryList = entryList;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public long getPreLogIndex() {
        return preLogIndex;
    }

    public void setPreLogIndex(long preLogIndex) {
        this.preLogIndex = preLogIndex;
    }

    public List<Entry> getLogEntryList() {
        return entryList;
    }

    public void setLogEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    @Override
    public String toString() {
        return "AppendEntriesMessage{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", preLogTerm=" + preLogTerm +
                ", preLogIndex=" + preLogIndex +
                ", entryList=" + entryList +
                '}';
    }
}
