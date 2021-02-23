package election.log.entry;

public class LogEntry extends Entry{
    private final byte[] commandBytes;

    public LogEntry(int kind, long term, long index, byte[] commandBytes) {
        super(kind, term, index);
        this.commandBytes = commandBytes;
    }
    public byte[] getCommandBytes() {
        return commandBytes;
    }
}
