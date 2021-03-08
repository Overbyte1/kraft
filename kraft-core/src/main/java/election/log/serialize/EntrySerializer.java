package election.log.serialize;

import election.log.entry.Entry;

public interface EntrySerializer {
    byte[] entryToBytes(Entry entry);
    Entry bytesToEntry(byte[] bytes);
}
