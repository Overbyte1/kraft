package server.store;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;

import static org.junit.Assert.*;

public class RocksDBKVStoreTest {
    private RocksDBKVStore rocksDBKVStore;
    @Before
    public void init() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        rocksDBKVStore = new RocksDBKVStore(options);
    }

    @Test
    public void del() {
        String key = "ertyuijh", value = "hello, rocksdb!";
        rocksDBKVStore.set(key, value.getBytes());
        assertEquals(true, rocksDBKVStore.containsKey(key));
        rocksDBKVStore.del(key);
        assertEquals(false, rocksDBKVStore.containsKey(key));
    }

    @Test
    public void containsKey() {
        String key = "ertyuijh", value = "hello, rocksdb!";
        rocksDBKVStore.del(key);
        assertEquals(false, rocksDBKVStore.containsKey(key));
        rocksDBKVStore.set(key, value.getBytes());
        assertEquals(true, rocksDBKVStore.containsKey(key));

    }

    @Test
    public void get() {
        String key = "ertyuijh", value = "hello, rocksdb!";
        rocksDBKVStore.set(key, value.getBytes());
        byte[] bytes = rocksDBKVStore.get(key);
        assertArrayEquals(value.getBytes(), bytes);
    }
    @After
    public void destroy() {
        rocksDBKVStore.close();
    }
}