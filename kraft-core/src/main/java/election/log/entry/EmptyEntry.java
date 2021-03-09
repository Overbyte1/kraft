package election.log.entry;

import election.log.serialize.EmptyEntrySerializer;
import election.log.serialize.EntrySerializerHandler;

public class EmptyEntry extends Entry  {
    public EmptyEntry(long term, long index) {
        super(EntryType.Empty, term, index);
    }

    @Override
    protected void registerSerializer() {
        EntrySerializerHandler.getInstance().register(EntryType.Empty, EmptyEntry.class, new EmptyEntrySerializer());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
