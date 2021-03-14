package log.serialize;

import log.entry.Entry;

public interface SerializableEntry {
    byte[] entryToBytes(Entry entry);
    Entry bytesToEntry(byte[] bytes);
}
