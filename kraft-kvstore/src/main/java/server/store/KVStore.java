package server.store;

public interface KVStore {
    void set(String key, byte[] value);
    void del(String key);
    boolean containsKey(String key);
    byte[] get(String key);
    void close();
}
