package log.serialize;

import log.store.EntryIndexItem;

public class EntryIndexSerializerImpl implements EntryIndexSerializer {
    @Override
    public byte[] entryIndexToBytes(EntryIndexItem entryIndexItem) {
        byte[] bytes = new byte[EntryIndexItem.BYTE_LEN];
        int idx = 0;
        idx = writeInt(bytes, idx, entryIndexItem.getType());
        idx = writeLong(bytes, idx, entryIndexItem.getTerm());
        idx = writeLong(bytes, idx, entryIndexItem.getIndex());
        idx = writeLong(bytes, idx, entryIndexItem.getOffset());
        return bytes;
    }
    @Override
    public EntryIndexItem bytesToEntryIndexItem(byte[] bytes) {
        int offset = 0;
        int type = ByteArrayConverter.readInt(bytes, 0);
        offset += 4;
        long term = ByteArrayConverter.readLong(bytes, offset);
        offset += 8;
        long index = ByteArrayConverter.readLong(bytes, offset);
        offset += 8;
        long fileOffset = ByteArrayConverter.readLong(bytes, offset);

        return new EntryIndexItem(type, index, term, fileOffset);
    }

    private int writeInt(byte[] b, int offset, int n) {
        return ByteArrayConverter.writeInt(b, offset, n);
    }
    private int writeLong(byte[] b, int offset, long n) {
        return ByteArrayConverter.writeLong(b, offset, n);
    }


}
