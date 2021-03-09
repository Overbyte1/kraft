package election.log.entry;

import election.log.serialize.EntrySerializerHandler;
import election.log.serialize.GeneralEntrySerializer;

import java.util.Arrays;

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

    @Override
    protected void registerSerializer() {
        EntrySerializerHandler.getInstance().register(EntryType.GENERAL, GeneralEntry.class, new GeneralEntrySerializer());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeneralEntry that = (GeneralEntry) o;

        return Arrays.equals(commandBytes, that.commandBytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(commandBytes);
    }
}
