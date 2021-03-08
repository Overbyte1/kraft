package election.log.entry;

import java.io.Serializable;

public abstract class Entry implements Serializable{
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


    @Override
    public String toString() {
        return "Entry{" +
                "type=" + type +
                ", term=" + term +
                ", index=" + index +
                '}';
    }
}
