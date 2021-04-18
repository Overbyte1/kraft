package server.store;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RocksDBKVStore implements KVStore {
    private static final Logger logger = LoggerFactory.getLogger(RocksDBKVStore.class);

    static {
        RocksDB.loadLibrary();
    }

    private final Options options;
    private RocksDB rocksDB;
    private final String DefaultPath = "./db/";

    public RocksDBKVStore(Options options, String dbPath) throws RocksDBException {
        this.options = options;
        rocksDB = RocksDB.open(options, dbPath);
    }
    public RocksDBKVStore(Options options) throws RocksDBException {
        this.options = options;
        rocksDB = RocksDB.open(options, DefaultPath);
    }

    @Override
    public void set(String key, byte[] value) {
        logger.debug("do set key/value: [{}]-[{}]", key, value);
        try {
            rocksDB.put(key.getBytes(), value);
        } catch (RocksDBException e) {
            logger.warn("fail to set key/value: [{}]-[{}], cause: {}", key, new String(value), e.getMessage());
        }
    }

    @Override
    public void del(String key) {
        logger.debug("do del, key: [{}]", key);
        try {
            rocksDB.delete(key.getBytes());
        } catch (RocksDBException e) {
            logger.warn("fail to set key: [{}], cause: {}", key, e.getMessage());
        }
    }

    @Override
    public boolean containsKey(String key) {
        return rocksDB.keyMayExist(key.getBytes(), null);
    }

    @Override
    public byte[] get(String key) {
        logger.debug("do get, key: [{}]", key);
        try {
            byte[] bytes =  rocksDB.get(key.getBytes());
            return bytes;
        } catch (RocksDBException e) {
            logger.warn("fail to get key: [{}], cause: {}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public void close() {
        rocksDB.close();
        options.close();
        logger.debug("rocksDB was closed!");

    }
}
