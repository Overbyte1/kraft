package server.store;

import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBTransactionKVStore implements TransactionKVStore {
    private static final Logger logger = LoggerFactory.getLogger(RocksDBTransactionKVStore.class);

    static {
        RocksDB.loadLibrary();
    }

    private final Options options;
    //private RocksDB transactionDb;
    private TransactionDB transactionDb;
    private final String DefaultPath = "./db/";
    TransactionDBOptions dbOptions = new TransactionDBOptions();

    public RocksDBTransactionKVStore(Options options, String dbPath) throws RocksDBException {
        this.options = options;
        //rocksDB = RocksDB.open(options, dbPath);

        //transactionDb = TransactionDB.open(options, dbPath);
        transactionDb = TransactionDB.open(options, dbOptions, dbPath);
    }
    public RocksDBTransactionKVStore(Options options) throws RocksDBException {
        this.options = options;
        transactionDb = TransactionDB.open(options, dbOptions, DefaultPath);
    }

    @Override
    public void set(String key, byte[] value) {
        logger.debug("do set key/value: [{}]-[{}]", key, value);
        try {
            transactionDb.put(key.getBytes(), value);
        } catch (RocksDBException e) {
            logger.warn("fail to set key/value: [{}]-[{}], cause: {}", key, new String(value), e.getMessage());
        }
    }

    @Override
    public void del(String key) {
        logger.debug("do del, key: [{}]", key);
        try {
            transactionDb.delete(key.getBytes());
        } catch (RocksDBException e) {
            logger.warn("fail to set key: [{}], cause: {}", key, e.getMessage());
        }
    }

    @Override
    public boolean containsKey(String key) {
        return transactionDb.keyMayExist(key.getBytes(), null);
    }

    @Override
    public byte[] get(String key) {
        logger.debug("do get, key: [{}]", key);
        try {
            byte[] bytes =  transactionDb.get(key.getBytes());
            return bytes;
        } catch (RocksDBException e) {
            logger.warn("fail to get key: [{}], cause: {}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public KVTransaction begin() {
        return new RocksDBKVTransaction(transactionDb.beginTransaction(new WriteOptions()));
    }


    @Override
    public void close() {
        transactionDb.close();
        dbOptions.close();
        options.close();
        logger.debug("rocksDB was closed!");

    }
}
