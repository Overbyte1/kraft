package election.log.store;

public class EntryFileMeta {
    public static final int LEN = 8;
    public static final long MAGIC = 0x8848;

    private long offset;

    public EntryFileMeta(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
}
