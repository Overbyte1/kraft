package server.store;

import org.junit.Test;
import org.rocksdb.*;

import java.io.IOException;

import static org.junit.Assert.*;

public class RocksDBTransactionKVStoreTest {

    @Test
    public void begin() throws RocksDBException, IOException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";

        KVTransaction transaction = kvStore.begin();
        transaction.set(key, value.getBytes());
        //commit
        transaction.commit();

        assertArrayEquals(value.getBytes(), kvStore.get(key));
        kvStore.del(key);
        assertArrayEquals(null, kvStore.get(key));

        transaction.close();
        kvStore.close();
        options.close();
    }

    @Test
    public void newIterator() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        String path = "./testRocksDbTrx/";
        //TransactionKVStore kvStore = new RocksDBTransactionKVStore(options, path);
        String key = "kk", value = "vv";

        //kvStore.set(key, value.getBytes());
        //TransactionDB transactionDB = TransactionDB.open(options,new TransactionDBOptions(), path);
        RocksDB rocksDB = RocksDB.open(options, path);
        RocksIterator iterator = rocksDB.newIterator();
        iterator.seekToFirst();
//        RocksIterator iterator = transactionDB.newIterator();

//        KVStoreIterator iterator = kvStore.newIterator();
//
        System.out.println(iterator.key() + " " + iterator.value());

    }
}