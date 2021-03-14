package log.entry;

import log.serialize.EntrySerializerHandler;

import java.io.Serializable;

public abstract class Entry implements Serializable{
    private transient static final int BYTE_LEN = 20; // 4 + 8 + 8
    private int type;
    private long term;
    private long index;


    public Entry(int type, long term, long index) {
        this.type = type;
        this.term = term;
        this.index = index;

        if(!EntrySerializerHandler.getInstance().isRegistered(this.getClass())) {
            registerSerializer();
        }
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

    protected abstract void registerSerializer();

    public static int getByteLen() {
        return BYTE_LEN;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (type != entry.type) return false;
        if (term != entry.term) return false;
        return index == entry.index;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (int) (term ^ (term >>> 32));
        result = 31 * result + (int) (index ^ (index >>> 32));
        return result;
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
