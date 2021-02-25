package election.node;

public class ReplicationState {
    private long nextIndex;
    private long matchIndex;

    public ReplicationState(long nextIndex, long matchIndex) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }

    public long getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(long matchIndex) {
        this.matchIndex = matchIndex;
    }
    public void incNextIndex(long n) {
        nextIndex += n;
    }
    public void incNextIndex() {
        incNextIndex(1);
    }
    public void decNextIndex(long n) {
        nextIndex -= n;
    }
    public void incMatchIndex(long n) {
        matchIndex += n;
    }
    public void incMatchIndex() {
        incMatchIndex(1);
    }
}
