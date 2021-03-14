package log.serialize;

import log.entry.Entry;
import log.entry.GeneralEntry;

import java.util.Arrays;

public class GeneralEntrySerializer extends AbstractSerializableEntry {
    @Override
    public byte[] entryToBytes(Entry entry) {
        byte[] superBytes = super.entryToBytes(entry);
        byte[] commandBytes = ((GeneralEntry) entry).getCommandBytes();
        byte[] res = new byte[superBytes.length + commandBytes.length];
        System.arraycopy(superBytes, 0, res, 0, superBytes.length);
        System.arraycopy(commandBytes, 0, res, superBytes.length, commandBytes.length);
        return res;
    }

    @Override
    public Entry bytesToEntry(byte[] bytes) {
        //跳过type字段
        int offset = Integer.BYTES;
        long index = ByteArrayConverter.readLong(bytes, offset);

        offset += Long.BYTES;
        long term = ByteArrayConverter.readLong(bytes, offset);

        offset += Long.BYTES;
        byte[] commandBytes = Arrays.copyOfRange(bytes, offset, bytes.length);

        return new GeneralEntry(term, index, commandBytes);
    }
}
