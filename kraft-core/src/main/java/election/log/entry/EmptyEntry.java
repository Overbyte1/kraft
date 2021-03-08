package election.log.entry;

public class EmptyEntry extends Entry  {
    public EmptyEntry(long term, long index) {
        super(EntryType.Empty, term, index);
    }

    @Override
    public byte[] entryToBytes(Entry entry) {
        return new byte[0];
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
        return null;
    }
}
