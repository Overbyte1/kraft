package election.log.serialize;

import election.log.entry.Entry;
import election.log.serialize.SerializableEntry;

public abstract class AbstractSerializableEntry implements SerializableEntry {
    @Override
    public byte[] entryToBytes(Entry entry) {
        byte[] bytes = new byte[Entry.getByteLen()];
        int offset = 0;
        offset = ByteArrayConverter.writeInt(bytes, offset, entry.getType());
        offset = ByteArrayConverter.writeLong(bytes, offset, entry.getIndex());
        ByteArrayConverter.writeLong(bytes, offset, entry.getTerm());
        return bytes;
    }


}
