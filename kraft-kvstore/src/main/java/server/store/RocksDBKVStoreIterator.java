package server.store;

import org.rocksdb.RocksIterator;
import server.store.KVStoreIterator;

public class RocksDBKVStoreIterator implements KVStoreIterator {
    private RocksIterator iterator;

    public RocksDBKVStoreIterator(RocksIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public void seekToFirst() {
        iterator.seekToFirst();
    }

    @Override
    public void seekToLast() {
        iterator.seekToLast();
    }

    @Override
    public byte[] key() {
        return iterator.key();
    }

    @Override
    public byte[] value() {
        return iterator.value();
    }

    @Override
    public void next() {
        iterator.next();
    }

    @Override
    public void prev() {
        iterator.prev();
    }

    @Override
    public boolean isValid() {
        return iterator.isValid();
    }
}
