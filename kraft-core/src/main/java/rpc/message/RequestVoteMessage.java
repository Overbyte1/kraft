package rpc.message;

import election.node.NodeId;

import java.io.Serializable;

public class RequestVoteMessage implements Serializable {
    private long term;
    private NodeId candidateId;
    private long lastLogIndex;
    private long lastLogTerm;

    public RequestVoteMessage() {
    }

    public RequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm) {
        this.term = term;
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(NodeId candidateId) {
        this.candidateId = candidateId;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestVoteMessage message = (RequestVoteMessage) o;

        if (term != message.term) return false;
        if (lastLogIndex != message.lastLogIndex) return false;
        if (lastLogTerm != message.lastLogTerm) return false;
        return candidateId != null ? candidateId.equals(message.candidateId) : message.candidateId == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (term ^ (term >>> 32));
        result = 31 * result + (candidateId != null ? candidateId.hashCode() : 0);
        result = 31 * result + (int) (lastLogIndex ^ (lastLogIndex >>> 32));
        result = 31 * result + (int) (lastLogTerm ^ (lastLogTerm >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "RequestVoteMessage{" +
                "term=" + term +
                ", candidateId=" + candidateId +
                ", lastLogIndex=" + lastLogIndex +
                ", lastLogTerm=" + lastLogTerm +
                '}';
    }
}
