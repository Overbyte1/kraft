package log.serialize;

import log.entry.Entry;

public interface EntrySerializer {
    byte[] entryToBytes(Entry entry);
    Entry bytesToEntry(byte[] bytes);
}
