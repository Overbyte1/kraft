package server.store;

import java.io.Closeable;

public interface KVStore extends Closeable {
    void set(String key, byte[] value);
    void del(String key);
    boolean containsKey(String key);
    byte[] get(String key);
    void close();
}
