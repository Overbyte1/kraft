package log.entry;

import log.serialize.EmptyEntrySerializer;
import log.serialize.EntrySerializerHandler;

public class EmptyEntry extends Entry  {
    public EmptyEntry(long term, long index) {
        super(EntryType.Empty, term, index);
    }

    public static void registerSerializer() {
        EntrySerializerHandler.getInstance().register(EntryType.Empty, EmptyEntry.class, new EmptyEntrySerializer());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
