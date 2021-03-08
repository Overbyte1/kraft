package election.log.entry;

public interface SerializableEntry {
    byte[] entryToBytes(Entry entry);
    Entry bytesToEntry(byte[] bytes);
}
