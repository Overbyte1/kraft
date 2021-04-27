package server.store;

import org.rocksdb.ReadOptions;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBKVTransaction implements  KVTransaction{
    private static final Logger logger = LoggerFactory.getLogger(RocksDBKVTransaction.class);

    private Transaction transaction;

    public RocksDBKVTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public void set(String key, byte[] value) {
        logger.debug("do set key/value: [{}]-[{}]", key, value);
        try {
            transaction.put(key.getBytes(), value);
        } catch (RocksDBException e) {
            logger.warn("fail to set key/value: [{}]-[{}], cause: {}", key, new String(value), e.getMessage());
        }
    }

    @Override
    public void del(String key) {
        logger.debug("do del, key: [{}]", key);
        try {
            transaction.delete(key.getBytes());
        } catch (RocksDBException e) {
            logger.warn("fail to set key: [{}], cause: {}", key, e.getMessage());
        }
    }

    @Override
    public boolean containsKey(String key) {
        try {
            return transaction.get(new ReadOptions(), key.getBytes()) != null;
        } catch (RocksDBException e) {
            logger.warn("fail to check containsKey key: [{}], cause: {}", key, e.getMessage());
        }
        return false;
    }

    @Override
    public byte[] get(String key) {
        logger.debug("do get, key: [{}]", key);
        try {
            byte[] bytes =  transaction.get(new ReadOptions(), key.getBytes());
            return bytes;
        } catch (RocksDBException e) {
            logger.warn("fail to get key: [{}], cause: {}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public void close() {
        transaction.close();
    }

    @Override
    public KVStoreIterator newIterator() {
        //TODO:隔离级别
        return new RocksDBKVStoreIterator(transaction.getIterator(new ReadOptions()));
    }


    @Override
    public void commit() throws RocksDBException {
        transaction.commit();
    }

    @Override
    public void rollback() throws RocksDBException {
        transaction.rollback();
    }
}
