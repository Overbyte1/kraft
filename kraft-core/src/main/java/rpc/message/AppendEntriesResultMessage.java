package rpc.message;

import java.io.Serializable;

public class AppendEntriesResultMessage implements Serializable {
    private long term;
    private boolean success;

    public AppendEntriesResultMessage(long term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "AppendEntriesResultMessage{" +
                "term=" + term +
                ", success=" + success +
                '}';
    }
}
