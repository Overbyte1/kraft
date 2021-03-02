package election.log.entry;

import java.io.Serializable;

public class EmptyEntry extends Entry implements Serializable {
    public EmptyEntry(long term, long index) {
        super(EntryType.Empty, term, index);
    }
}
