package log.store;

import log.entry.Entry;
import log.serialize.EntrySerializerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class EntryDataFile {
    private static final Logger logger = LoggerFactory.getLogger(EntryDataFile.class);

    private final String filename;
    private RandomAccessFile randomAccessFile;
    private EntryFileMeta entryFileMeta;
    private static final String openMode = "rws";
    //private static final int indexByteLen = 8;


    private EntrySerializerHandler entrySerializerHandler = EntrySerializerHandler.getInstance();
    //当前文件最后一个Entry的index
    private long lastEntryIndex;

    public EntryDataFile(File file)
            throws IOException {

        filename = file.getName();
        init(file);
    }
    private void init(File file) throws IOException {
        //File file = new File(fileName);
        if(!file.exists()) {
            file.createNewFile();
        }
        randomAccessFile = new RandomAccessFile(file, openMode);
        if(randomAccessFile.length() < EntryFileMeta.LEN) {
            entryFileMeta = new EntryFileMeta(0);

            randomAccessFile.writeLong(entryFileMeta.MAGIC);
            randomAccessFile.writeLong(entryFileMeta.getOffset());

            logger.debug("entry index file: {}, the file length is less than {}, rewrite meta data:" +
                            " [magic: {}, index offset: {}]",
                    filename, EntryIndexFileMeta.LEN, entryFileMeta.MAGIC, entryFileMeta.getOffset());
            //刚创建时还没有保存entry，当前文件保存entry的index为-1，表示为空
            //randomAccessFile.writeLong(-1);
        } else {
            long magic = randomAccessFile.readLong();
            if(magic != EntryFileMeta.MAGIC) {
                logger.warn("entry data file: {}, illegal magic number {}, it should be {}",
                        filename, magic, EntryFileMeta.MAGIC);
                throw new FileFormatNotSupportException("magic of file should be: "
                        + EntryFileMeta.MAGIC + ", but found: " + magic);
            }
            long offset = randomAccessFile.readLong();
            entryFileMeta = new EntryFileMeta(offset);
        }
    }

    /**
     * 获取当前文件大小
     * @return
     * @throws IOException
     */
    public long getSize() throws IOException {
        return randomAccessFile.length();
    }


    /**
     * 从尾部写入一个Entry
     * @param entry
     * @return 该entry在文件中的偏移量
     * @throws IOException
     */
    public long appendEntry(Entry entry) throws IOException {
        long offset = randomAccessFile.length();
        byte[] bytes = entrySerializerHandler.entryToBytes(entry);
        randomAccessFile.seek(offset);
        //写入entry的空间大小
        randomAccessFile.writeInt(bytes.length);
        //写入entry数据
        randomAccessFile.write(bytes);
        logger.debug("entry {} was append to entry data file: {}, start file offset: {}", entry, filename, offset);
        //写入最后一个entry的index
//        lastEntryIndex = entry.getIndex();
//        randomAccessFile.writeLong(lastEntryIndex);
        return offset;
    }

    /**
     * 指定偏移量读某个Entry
     * @param offset
     * @return 该偏移量处的entry
     * @throws IOException
     */
    public Entry getEntry(long offset) throws IOException {
        if(offset > randomAccessFile.length()) {
            return null;
        }
        randomAccessFile.seek(offset);
        int size = randomAccessFile.readInt();
        byte[] bytes = new byte[size];
        randomAccessFile.read(bytes);
        Entry entry = entrySerializerHandler.bytesToEntry(bytes);
        logger.debug("entry {} was read from file {}, file offset: {}", entry, filename, offset);
        return entry;
    }
    public boolean deleteFromOffset(long offset) throws IOException {
        if(offset >= randomAccessFile.length()) {
            return false;
        }
        randomAccessFile.setLength(offset);
        logger.debug("delete entries from file offset {}, entry data file is {}", offset, filename);
        return true;
    }

    public String getFilename() {
        return filename;
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }

}
