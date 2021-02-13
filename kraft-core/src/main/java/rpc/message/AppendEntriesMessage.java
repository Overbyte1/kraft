package rpc.message;

import election.node.NodeId;
import election.log.LogEntry;

import java.util.List;

public class AppendEntriesMessage {
    //Leader的term
    private long term;
    private NodeId leaderId;
    //Leader的上一个日志所属的term
    private long preLogTerm;
    //Leader的上一个日志的index
    private long preLogIndex;
    //发送的日志，为了提高效率可能一次性发送多个
    private List<LogEntry> logEntryList;

    public AppendEntriesMessage(long term, NodeId leaderId, long preLogTerm, long preLogIndex, List<LogEntry> logEntryList) {
        this.term = term;
        this.leaderId = leaderId;
        this.preLogTerm = preLogTerm;
        this.preLogIndex = preLogIndex;
        this.logEntryList = logEntryList;
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

    public List<LogEntry> getLogEntryList() {
        return logEntryList;
    }

    public void setLogEntryList(List<LogEntry> logEntryList) {
        this.logEntryList = logEntryList;
    }

    @Override
    public String toString() {
        return "AppendEntriesMessage{" +
                "term=" + term +
                ", leaderId=" + leaderId +
                ", preLogTerm=" + preLogTerm +
                ", preLogIndex=" + preLogIndex +
                ", logEntryList=" + logEntryList +
                '}';
    }
}
