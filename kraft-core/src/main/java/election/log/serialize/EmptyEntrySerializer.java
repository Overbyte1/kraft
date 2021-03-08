package election.log.serialize;

import election.log.entry.Entry;
import election.log.entry.SerializableEntry;

public class EmptyEntrySerializer implements SerializableEntry {
    @Override
    public byte[] entryToBytes(Entry entry) {
        return new byte[0];
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
        return null;
    }
}
