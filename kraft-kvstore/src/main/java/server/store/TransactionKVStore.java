package server.store;

public interface TransactionKVStore extends KVStore {
    KVTransaction begin();
}
