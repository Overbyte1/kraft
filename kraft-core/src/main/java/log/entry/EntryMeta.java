package log.entry;

public class EntryMeta {
    private long logIndex;
    private long term;

    public EntryMeta(long preLogIndex, long term) {
        this.logIndex = preLogIndex;
        this.term = term;
    }

    public long getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}
