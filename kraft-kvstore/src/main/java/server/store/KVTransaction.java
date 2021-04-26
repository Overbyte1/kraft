package server.store;

import org.rocksdb.RocksDBException;

public interface KVTransaction extends KVStore {
    void commit() throws RocksDBException;
    void rollback() throws RocksDBException;
}
