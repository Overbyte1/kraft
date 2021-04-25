package server.store;

import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;

public interface TransactionKVStore extends KVStore {
    Transaction begin();
    void commit(Transaction transaction) throws RocksDBException;
    void rollback(Transaction transaction) throws RocksDBException;
}
