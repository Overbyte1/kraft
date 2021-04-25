package server.store;

import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

import static org.junit.Assert.*;

public class RocksDBTransactionKVStoreTest {

    @Test
    public void begin() throws RocksDBException {

    }

    @Test
    public void commit() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";
        Transaction transaction = kvStore.begin();
        kvStore.set(key, value.getBytes());
        //
        kvStore.commit(transaction);

        assertArrayEquals(value.getBytes(), kvStore.get(key));
        kvStore.del(key);
        assertArrayEquals(null, kvStore.get(key));
        kvStore.close();
    }

    @Test
    public void rollback() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";
        Transaction transaction = kvStore.begin();
        kvStore.set(key, value.getBytes());
        //TODO:封装一个Transaction
        kvStore.rollback(transaction);

        byte[] bytes = kvStore.get(key);
        System.out.println(new String(bytes));
        kvStore.close();
    }
}