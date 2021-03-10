package election.log.serialize;

public class ByteArrayConverter {

    public static int writeInt(byte[] b, int offset, int n) {
        int mask = 0xFF;
        b[offset++] = (byte)(mask & n);
        b[offset++] = (byte)(mask & (n << 8));
        b[offset++] = (byte)(mask & (n << 16));
        b[offset++] = (byte)(mask & (n << 24));
        return offset;
    }

    public static int writeLong(byte[] b, int offset, long n) {
        int mask = 0xFFFFFFFF;
        int idx = writeInt(b, offset, (int)(n & mask));
        return writeInt(b, idx, (int)((n >> 32) & mask));
    }

    public static int readInt(byte[] b, int offset) {
        int ret = 0;
        ret |= b[offset++];
        ret |= (b[offset++] << 8);
        ret |= (b[offset++] << 16);
        ret |= (b[offset++] << 24);
        return ret;
    }

    public static long readLong(byte[] b, int offset) {
        int low = readInt(b, offset);
        int high = readInt(b, offset + Integer.BYTES);
        return low | (high << 32);
    }
}
