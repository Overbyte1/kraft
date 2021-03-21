package log.store;

import log.serialize.EntryIndexSerializer;
import log.serialize.EntryIndexSerializerImpl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Entry文件索引，记录index对应日志的文件偏移值
 */
public class EntryIndexFile {
    private RandomAccessFile randomAccessFile;
    private EntryIndexFileMeta entryIndexFileMeta;
    private static final String openMode = "rws";
    //记录每个EntryIndexItem的长度，单位为字节
    private static final int ITEM_LENGTH_BYTE = 4;
    //private static final int indexByteLen = 8;

    private final EntryIndexSerializer entryIndexSerializer = new EntryIndexSerializerImpl();
    //private long startEntryIndex;

    private EntryIndexItem lastEntryIndexItem;

    public EntryIndexFile(File file, long startEntryIndex, long termOffset) throws IOException {
        init(file, startEntryIndex, termOffset);

    }
    public EntryIndexFile(File file) throws IOException {
        randomAccessFile = new RandomAccessFile(file, openMode);
        initEntryIndexFileMeta();
        long fileLen = randomAccessFile.length();

        assert isLengthLegal(randomAccessFile.length());
        //清除不完整数据
        clearIncompleteData();

        //preIndex是上一个文件最后的index，如果当前文件是第0个文件，没有前一个文件了，
        // 此时entryIndexFileMeta.getPreIndex()==0
        lastEntryIndexItem = getEntryIndexItemByFileOffset(fileLen - getEntryIndexItemStoreByte());
    }


    /**
     * 判断文件的长度是否合法，当系统崩溃时可能导致只写入了部分数据，所以需要加以检测
     * @param fileLen 文件长度
     * @return
     */
    private boolean isLengthLegal(long fileLen) {
        return (fileLen - EntryIndexFileMeta.getLEN()) % getEntryIndexItemStoreByte() == 0;
    }

    /**
     * 系统崩溃可能会导致最后一个EntryIndexItem写入不完整，这部分不完整的数据需要清除
     * @throws IOException
     */
    private void clearIncompleteData() throws IOException {
        long fileLen = randomAccessFile.length();
        if(!isLengthLegal(fileLen)) {
            long lastByte = (fileLen - EntryIndexFileMeta.getLEN()) % getEntryIndexItemStoreByte();
            randomAccessFile.setLength(fileLen - lastByte);
        }
    }

    /**
     * 获取EntryIndexItem在存储时实际需要的空间，单位为字节。每个item在存储时需要先写入其长度再写入其序列化得到的
     * 字节数组
     * @return
     */
    private int getEntryIndexItemStoreByte() {
        return ITEM_LENGTH_BYTE + EntryIndexItem.BYTE_LEN;
    }

    //TODO:提到抽象类
    private void init(File file, long indexOffset, long termOffset) throws IOException {
        //File file = new File(fileName);
        if(!file.exists()) {
            file.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(file, openMode);
        long fileLen = randomAccessFile.length();
        if(fileLen < EntryIndexFileMeta.LEN) {
            entryIndexFileMeta = new EntryIndexFileMeta(indexOffset, termOffset);

            randomAccessFile.writeLong(entryIndexFileMeta.MAGIC);
            randomAccessFile.writeLong(indexOffset);
            randomAccessFile.writeLong(termOffset);
        } else if(fileLen >= EntryIndexFileMeta.LEN){
            initEntryIndexFileMeta();

            if(fileLen > EntryIndexFileMeta.LEN) {

                assert isLengthLegal(randomAccessFile.length());

                lastEntryIndexItem = getEntryIndexItemByFileOffset(fileLen - getEntryIndexItemStoreByte());
            }
        }
    }

    private void initEntryIndexFileMeta() throws IOException {
        long magic = randomAccessFile.readLong();
        if(magic != EntryIndexFileMeta.MAGIC) {
            throw new FileFormatNotSupportException("magic of file should be: "
                    + EntryFileMeta.MAGIC + ", but found: " + magic);
        }
        long startIndex = randomAccessFile.readLong();
        long startTerm = randomAccessFile.readLong();
        entryIndexFileMeta = new EntryIndexFileMeta(startIndex, startTerm);
    }

    public boolean appendEntryIndexItem(EntryIndexItem entryIndexItem) throws IOException {
//        if(!isMatch(entryIndexItem.getIndex(), entryIndexItem.getTerm())) {
//            return false;
//        }

        byte[] bytes = entryIndexSerializer.entryIndexToBytes(entryIndexItem);
        randomAccessFile.seek(randomAccessFile.length());
        //System.out.println(randomAccessFile.length() + " " + entryIndexItem);
        //写入entry的空间大小
        randomAccessFile.writeInt(bytes.length);
        //写入entry数据
        randomAccessFile.write(bytes);

        lastEntryIndexItem = entryIndexItem;

        return true;
        //写入最后一个entry的index
//        lastEntryIndex = entryIndexItem.getIndex();
//        lastEntryTerm = entryIndexItem.getTerm();
        //randomAccessFile.writeLong(lastEntryIndex);
    }

    public EntryIndexItem getEntryIndexItem(long entryIndex) throws IOException {
        if(entryIndex <= entryIndexFileMeta.getPreIndex() || entryIndex > lastEntryIndexItem.getIndex()) {
            return null;
        }
        long  offset = getEntryOffset(entryIndex);
        return getEntryIndexItemByFileOffset(offset);
    }

    public EntryIndexItem getEntryIndexItemByFileOffset(long fileOffset) throws IOException {
        randomAccessFile.seek(fileOffset);
        int len = randomAccessFile.readInt();
        byte[] bytes = new byte[len];
        randomAccessFile.read(bytes);

        return entryIndexSerializer.bytesToEntryIndexItem(bytes);
    }
    public EntryIndexItem getPreEntryIndexItem(long entryIndex) throws IOException {
        if(entryIndex - 1 == entryIndexFileMeta.getPreIndex()) {
            return new EntryIndexItem(-1, entryIndexFileMeta.getPreIndex(),
                    entryIndexFileMeta.getPreTerm(), -1);
        }
        return getEntryIndexItem(entryIndex - 1);
    }

    public EntryIndexItem getLastEntryIndexItem() {
        return lastEntryIndexItem;
    }

    public boolean deleteEntriesFrom(long logIndex) throws IOException {
        long fileOffset = getEntryOffset(logIndex);
        if(fileOffset >= randomAccessFile.length()) {
            return false;
        }
        randomAccessFile.seek(fileOffset);
        randomAccessFile.setLength(fileOffset);
        return true;
    }

    public boolean isEmpty() {
        return lastEntryIndexItem == null;
    }

    private long getEntryOffset(long entryIndex) {
        return (entryIndex - entryIndexFileMeta.getPreIndex() - 1) * (EntryIndexItem.getByteLen() + ITEM_LENGTH_BYTE)
                + EntryIndexFileMeta.LEN;
    }
    private boolean isMatch(long preIndex, long preTerm) {
        if(lastEntryIndexItem != null) {
            return lastEntryIndexItem.getIndex() == preIndex && lastEntryIndexItem.getTerm() == preTerm;
        }
        return entryIndexFileMeta.getPreIndex() == preIndex && entryIndexFileMeta.getPreTerm() == preTerm;
    }
    public void close() throws IOException {
        randomAccessFile.close();
    }

}
