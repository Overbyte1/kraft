package election.node;

import election.exception.IndexException;

public class ReplicationState {
    private long nextIndex;
    private long matchIndex;
    private boolean replicating;
    private long lastReplicationTime;

    public ReplicationState(long nextIndex, long matchIndex) {
        this(nextIndex, matchIndex, true, 0);
    }

    public ReplicationState(long nextIndex, long matchIndex, boolean replicating, long lastReplicationTime) {
        this.nextIndex = nextIndex;
        this.matchIndex = matchIndex;
        this.replicating = replicating;
        this.lastReplicationTime = lastReplicationTime;
    }

    public long getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(long nextIndex) {
        this.nextIndex = nextIndex;
    }

    public void startReplication() {
        replicating = true;
    }
    public void stopReplication() {
        replicating = false;
    }
    public boolean isReplicating() {
        return replicating;
    }

    public long getLastReplicationTime() {
        return lastReplicationTime;
    }

    public void setLastReplicationTime(long lastReplicationTime) {
        this.lastReplicationTime = lastReplicationTime;
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
        long idx = nextIndex - n;
        if(idx <= 0) {
            throw new IndexException("index: " + idx);
        }
        nextIndex = idx;
    }
    public void incMatchIndex(long n) {
        matchIndex += n;
    }
    public void incMatchIndex() {
        incMatchIndex(1);
    }
}
