package election.log.entry;

public class GeneralEntry extends Entry{
    private final byte[] commandBytes;

    public GeneralEntry(long term, long index, byte[] commandBytes) {
        super(EntryType.GENERAL, term, index);
        this.commandBytes = commandBytes;
    }

    public GeneralEntry(long term, byte[] commandBytes) {
        this(term, -1, commandBytes);
    }

    public byte[] getCommandBytes() {
        return commandBytes;
    }
}
