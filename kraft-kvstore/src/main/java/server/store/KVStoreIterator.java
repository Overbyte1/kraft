package server.store;

public interface KVStoreIterator {
    void seekToFirst();
    void seekToLast();
    byte[] key();
    byte[] value();
    void next();
    void prev();
    boolean isValid();
}
