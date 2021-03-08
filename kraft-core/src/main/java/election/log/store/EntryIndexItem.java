package election.log.store;

public class EntryIndexItem {
    public static final int BYTE_LEN = 12;
    private int type;
    private long index;
    private long term;
    private long offset;


    public EntryIndexItem(int type, long index, long term, long offset) {
        this.type = type;
        this.index = index;
        this.term = term;
        this.offset = offset;
    }

    public static int getByteLen() {
        return BYTE_LEN;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
