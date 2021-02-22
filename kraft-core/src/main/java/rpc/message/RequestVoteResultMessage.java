package rpc.message;

import java.io.Serializable;

public class RequestVoteResultMessage implements Serializable {
    private long term;
    private boolean voteGranted;

    public RequestVoteResultMessage(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    @Override
    public String toString() {
        return "RequestVoteResultMessage{" +
                "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }
}
