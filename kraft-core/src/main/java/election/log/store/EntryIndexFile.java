package election.log.store;

import election.log.serialize.EntryIndexSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Entry文件索引，记录index对应日志的文件偏移值
 */
public class EntryIndexFile {
    private RandomAccessFile randomAccessFile;
    private EntryIndexFileMeta entryIndexFileMeta;
    private static final String openMode = "rw";
    //private static final int indexByteLen = 8;

    private EntryIndexSerializer entryIndexSerializer;
    //private long startEntryIndex;

    private EntryIndexItem lastEntryIndexItem;

    public EntryIndexFile(File file, long startEntryIndex, long termOffset) throws IOException {
        init(file, startEntryIndex, termOffset);
    }
    public EntryIndexFile(File file) throws IOException {
        randomAccessFile = new RandomAccessFile(file, openMode);
        long fileLen = randomAccessFile.length();

        assert (fileLen - EntryIndexFileMeta.LEN)  % EntryIndexItem.BYTE_LEN == 0;

        lastEntryIndexItem = getEntryIndexItem((fileLen - EntryIndexFileMeta.LEN) / EntryIndexItem.BYTE_LEN);
    }

    //TODO:提到抽象类
    private void init(File file, long indexOffset, long termOffset) throws IOException {
        //File file = new File(fileName);
        if(!file.exists()) {
            file.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(file, openMode);
        long fileLen = randomAccessFile.length();
        if(fileLen < EntryFileMeta.LEN) {
            entryIndexFileMeta = new EntryIndexFileMeta(indexOffset, termOffset);

            randomAccessFile.writeLong(entryIndexFileMeta.MAGIC);
            randomAccessFile.writeLong(indexOffset);
            randomAccessFile.writeLong(termOffset);
        } else if(fileLen > EntryFileMeta.LEN){
            long magic = randomAccessFile.readLong();
            if(magic != EntryFileMeta.MAGIC) {
                throw new FileFormatUnSupportException("magic of file should be: "
                        + EntryFileMeta.MAGIC + ", but found: " + magic);
            }
            long startIndex = randomAccessFile.readLong();
            long startTerm = randomAccessFile.readLong();
            entryIndexFileMeta = new EntryIndexFileMeta(startIndex, startTerm);
        }
    }

    public boolean appendEntryIndexItem(EntryIndexItem entryIndexItem) throws IOException {
        if(!isMatch(entryIndexItem.getIndex(), entryIndexItem.getTerm())) {
            return false;
        }
        byte[] bytes = entryIndexSerializer.entryIndexToBytes(entryIndexItem);
        randomAccessFile.seek(randomAccessFile.length());
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
        if(entryIndex < entryIndexFileMeta.getPreIndex() || entryIndex < lastEntryIndexItem.getIndex()) {
            return null;
        }
        long  offset = getEntryOffset(entryIndex);
        randomAccessFile.seek(offset);
        int len = randomAccessFile.readInt();
        byte[] bytes = new byte[len];
        randomAccessFile.read(bytes);

        return entryIndexSerializer.bytesToEntryIndexItem(bytes);
    }

    public EntryIndexItem getLastEntryIndexItem() {
        return lastEntryIndexItem;
    }

    public boolean isEmpty() {
        return lastEntryIndexItem == null;
    }

    private long getEntryOffset(long entryIndex) {
        return (entryIndex - entryIndexFileMeta.getPreIndex()) * EntryIndexItem.getByteLen()
                + EntryIndexFileMeta.LEN;
    }
    private boolean isMatch(long preIndex, long preTerm) {
        if(lastEntryIndexItem != null) {
            return lastEntryIndexItem.getIndex() == preIndex && lastEntryIndexItem.getTerm() == preTerm;
        }
        return entryIndexFileMeta.getPreIndex() == preIndex && entryIndexFileMeta.getPreTerm() == preTerm;
    }

}