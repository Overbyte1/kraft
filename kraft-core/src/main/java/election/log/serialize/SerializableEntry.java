package election.log.serialize;

import election.log.entry.Entry;

public interface SerializableEntry {
    byte[] entryToBytes(Entry entry);
    Entry bytesToEntry(byte[] bytes);
}
