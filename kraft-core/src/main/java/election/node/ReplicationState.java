package election.node;

public class ReplicationState {
    private int nextIndex;
    private int matchIndex;

    public int getNextIndex() {
        return nextIndex;
    }

    public void setNextIndex(int nextIndex) {
        this.nextIndex = nextIndex;
    }

    public int getMatchIndex() {
        return matchIndex;
    }

    public void setMatchIndex(int matchIndex) {
        this.matchIndex = matchIndex;
    }
}
