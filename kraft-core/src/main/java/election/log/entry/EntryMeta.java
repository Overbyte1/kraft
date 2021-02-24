package election.log.entry;

public class EntryMeta {
    private long preLogIndex;
    private long term;

    public EntryMeta(long preLogIndex, long term) {
        this.preLogIndex = preLogIndex;
        this.term = term;
    }

    public long getPreLogIndex() {
        return preLogIndex;
    }

    public void setPreLogIndex(long preLogIndex) {
        this.preLogIndex = preLogIndex;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}
