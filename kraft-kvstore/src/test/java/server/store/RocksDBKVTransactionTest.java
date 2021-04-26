package server.store;

import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;

import static org.junit.Assert.*;

public class RocksDBKVTransactionTest {

    @Test
    public void commit() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";

        KVTransaction transaction = kvStore.begin();
        transaction.set(key, value.getBytes());
        assertArrayEquals(value.getBytes(), transaction.get(key));
        transaction.commit();

        assertArrayEquals(value.getBytes(), kvStore.get(key));
        kvStore.del(key);
        assertArrayEquals(null, kvStore.get(key));

        transaction.close();
        kvStore.close();
        options.close();
    }

    @Test
    public void rollback() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";

        KVTransaction transaction = kvStore.begin();
        transaction.set(key, value.getBytes());
        assertArrayEquals(value.getBytes(), transaction.get(key));
        //rollback
        transaction.rollback();

        assertArrayEquals(null, kvStore.get(key));
        kvStore.del(key);
        assertArrayEquals(null, kvStore.get(key));

        transaction.close();
        kvStore.close();
        options.close();
    }
}