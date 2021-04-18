package server.store;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemHTKVStore implements KVStore {
    private Map<String, byte[]> storeMap = new ConcurrentHashMap<>();

    public void set(String key, byte[] value) {
        storeMap.put(key, value);
    }

    @Override
    public void del(String key) {
        storeMap.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return storeMap.containsKey(key);
    }

    @Override
    public byte[] get(String key) {
        return storeMap.get(key);
    }

    @Override
    public void close() {

    }
}
