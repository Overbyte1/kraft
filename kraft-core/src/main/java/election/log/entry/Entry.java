package election.log.entry;

public abstract class Entry {
    private int kind;
    private long term;
    private long index;

    public Entry(int kind, long term, long index) {
        this.kind = kind;
        this.term = term;
        this.index = index;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }
}
