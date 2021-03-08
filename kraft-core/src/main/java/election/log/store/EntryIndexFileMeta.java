package election.log.store;

public class EntryIndexFileMeta {
    public static final int LEN = 8;
    public static final long MAGIC = 0x8848;

    private long preIndex;
    private long preTerm;

    public EntryIndexFileMeta(long preIndex, long preTerm) {
        this.preIndex = preIndex;
        this.preTerm = preTerm;
    }
    public EntryIndexFileMeta() {}

    public static int getLEN() {
        return LEN;
    }

    public static long getMAGIC() {
        return MAGIC;
    }

    public long getPreIndex() {
        return preIndex;
    }

    public void setPreIndex(long preIndex) {
        this.preIndex = preIndex;
    }

    public long getPreTerm() {
        return preTerm;
    }

    public void setPreTerm(long preTerm) {
        this.preTerm = preTerm;
    }
}
