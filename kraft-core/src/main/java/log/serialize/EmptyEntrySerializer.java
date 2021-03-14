package log.serialize;

import log.entry.EmptyEntry;
import log.entry.Entry;

public class EmptyEntrySerializer extends AbstractSerializableEntry {
    @Override
    public byte[] entryToBytes(Entry entry) {
        return super.entryToBytes(entry);
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
        //跳过type字段
        int offset = Integer.BYTES;
        long index = ByteArrayConverter.readLong(bytes, offset);

        offset += Long.BYTES;
        long term = ByteArrayConverter.readLong(bytes, offset);

        return new EmptyEntry(index, term);
    }
}
