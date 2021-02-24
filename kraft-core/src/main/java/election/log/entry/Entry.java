package election.log.entry;

public abstract class Entry {
    private int type;
    private long term;
    private long index;


    public Entry(int type, long term, long index) {
        this.type = type;
        this.term = term;
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
