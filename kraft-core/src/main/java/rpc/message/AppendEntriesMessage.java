package rpc.message;

import log.entry.Entry;
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
    private long leaderCommit;
    //发送的日志，为了提高效率可能一次性发送多个
    private List<Entry> entryList;

    public AppendEntriesMessage(long term, NodeId leaderId, long preLogTerm, long preLogIndex, long leaderCommit, List<Entry> entryList) {
        this.term = term;
        this.leaderId = leaderId;
        this.preLogTerm = preLogTerm;
        this.preLogIndex = preLogIndex;
        this.leaderCommit = leaderCommit;
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

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }

    public List<Entry> getEntryList() {
        return entryList;
    }

    public void setEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    public List<Entry> getLogEntryList() {
        return entryList;
    }

    public void setLogEntryList(List<Entry> entryList) {
        this.entryList = entryList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppendEntriesMessage message = (AppendEntriesMessage) o;

        if (term != message.term) return false;
        if (preLogTerm != message.preLogTerm) return false;
        if (preLogIndex != message.preLogIndex) return false;
        if (leaderCommit != message.leaderCommit) return false;
        if (leaderId != null ? !leaderId.equals(message.leaderId) : message.leaderId != null) return false;
        return entryList != null ? entryList.equals(message.entryList) : message.entryList == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (term ^ (term >>> 32));
        result = 31 * result + (leaderId != null ? leaderId.hashCode() : 0);
        result = 31 * result + (int) (preLogTerm ^ (preLogTerm >>> 32));
        result = 31 * result + (int) (preLogIndex ^ (preLogIndex >>> 32));
        result = 31 * result + (int) (leaderCommit ^ (leaderCommit >>> 32));
        result = 31 * result + (entryList != null ? entryList.hashCode() : 0);
        return result;
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
