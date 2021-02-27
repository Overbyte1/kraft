package election.log.entry;

public class EmptyEntry extends Entry {
    public EmptyEntry(long term, long index) {
        super(EntryType.Empty, term, index);
    }
}
