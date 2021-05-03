package server.store;

import java.io.Closeable;

public interface KVStoreIterator extends Closeable {
    void seekToFirst();
    void seekToLast();
    byte[] key();
    byte[] value();
    void next();
    void prev();
    boolean isValid();
}
