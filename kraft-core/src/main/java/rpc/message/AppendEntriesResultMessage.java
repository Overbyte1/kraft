package rpc.message;

import java.io.Serializable;

public class AppendEntriesResultMessage implements Serializable {
    private long term;
    private boolean success;
    //持久化的日志条数
    private int logNum;

    public AppendEntriesResultMessage(long term, boolean success) {
        this.term = term;
        this.success = success;
        if(success) {
            logNum = 1;
        } else {
            logNum = 0;
        }
    }

    public AppendEntriesResultMessage(long term, boolean success, int logNum) {
        this.term = term;
        this.success = success;
        this.logNum = logNum;
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

    public int getLogNum() {
        return logNum;
    }

    public void setLogNum(int logNum) {
        this.logNum = logNum;
    }

    @Override
    public String toString() {
        return "AppendEntriesResultMessage{" +
                "term=" + term +
                ", success=" + success +
                ", logNum=" + logNum +
                '}';
    }
}
