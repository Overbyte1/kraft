package log.store;

import log.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 基于文件进行存储，需要考虑：
 * 1. 文件的存储格式、位置、命名
 * 2. 写回文件，读取文件的时机
 *
 */
public class FileLogStore extends AbstractLogStore implements LogStore {
    private static final Logger logger = LoggerFactory.getLogger(FileLogStore.class);

    private EntryDataFile entryDataFile;
    private EntryIndexFile entryIndexFile;
    private EntryGenerationHandler generationHandler;

    //buffer容量，单位为字节
    private static final int DEFAULT_MAX_FILE_SIZE = 1024 * 1024;
    //数据文件的最大大小，单位为KB
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024 * 1024;

    private final int maxFileSize;

    private final EntryBuffer buffer;

    /**
     * 构造方法
     * @param path entry（日志）文件路径
     * @param bufferSize buffer容量，单位是字节
     * @param maxFileSize 日志文件的最大大小，单位为 KB
     * @throws IOException
     */
    public FileLogStore(String path, int bufferSize, int maxFileSize) throws IOException {
        mkdir(path);

        generationHandler = new EntryGenerationHandler(path);
        EntryGeneration latestGeneration = generationHandler.getLatestGeneration();
        entryDataFile = latestGeneration.getEntryDataFile();
        entryIndexFile = latestGeneration.getEntryIndexFile();

        EntryIndexItem lastEntryIndexItem = entryIndexFile.getLastEntryIndexItem();
        if(lastEntryIndexItem != null) {
            lastLogIndex = lastEntryIndexItem.getIndex();
        } else {
            lastLogIndex = 0;
        }

        this.maxFileSize = maxFileSize;
        buffer = new EntryBuffer(bufferSize);
    }

    public FileLogStore(String path) throws IOException {
        this(path, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_FILE_SIZE);
    }

    /**
     * 构造方法
     * @param path entry文件目录
     * @param bufferSize buffer容量，单位为字节
     * @throws IOException
     */
    public FileLogStore(String path, int bufferSize) throws IOException {
        this(path, bufferSize, DEFAULT_MAX_FILE_SIZE);
    }

    private void mkdir(String path) {
        File file = new File(path);
        if(!file.exists()) {
            logger.debug("directory: [{}] was created", file.getAbsolutePath());
            file.mkdirs();
        }
    }

    @Override
    public Entry getLogEntry(long logIndex) {
        //先找buffer，若找不到就去找当前Generation，否则进行二分查找所有文件
        if(logIndex > lastLogIndex) {
            return null;
        }
        if(buffer.contains(logIndex)) {
            return buffer.getEntry(logIndex);
        }
        try {
            EntryIndexItem entryIndexItem = entryIndexFile.getEntryIndexItem(logIndex);
            if(entryIndexItem != null) {
                return entryDataFile.getEntry(entryIndexItem.getOffset());
            }
            //还是二分查找，根据每个索引文件包含的最大index进行二分，直到定位到包含索引logIndex的文件
            int currentGenerationIndex = generationHandler.getCurrentGenerationIndex();
            //如果只有一个文件，说明找不到该index对应的entry
            if(currentGenerationIndex == 0) {
                return null;
            }
            logger.debug("entry index {} was not found in current generation, start finding another generation file......", logIndex);

            int low = 0, high = currentGenerationIndex - 1, mid;
            //EntryGeneration midGeneration;
            while(low < high) {
                mid = low + (high - low) / 2;

                try(EntryGeneration midGeneration = generationHandler.getGeneration(mid)) {
                    if (midGeneration == null) {
                        return null;
                    }
                    EntryIndexItem midIndexItem = midGeneration.getEntryIndexFile().getLastEntryIndexItem();

                    if (logIndex > midIndexItem.getIndex()) {
                        low = mid + 1;
                    } else if (logIndex < midIndexItem.getIndex()) {
                        high = mid;
                    } else {
                        low = mid;
                        break;
                    }
                }
            }
            logger.debug("the entry index {} is in {}th generation", logIndex, low);

            try (EntryGeneration generation = generationHandler.getGeneration(low)) {
                EntryIndexItem targetIndexItem = generation.getEntryIndexFile().getEntryIndexItem(logIndex);
                if (targetIndexItem != null) {
                    return generation.getEntryDataFile().getEntry(targetIndexItem.getOffset());
                }
            }
        } catch (IOException e) {
            logger.warn("fail to read entry, exception message: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public boolean appendEmptyEntry(Entry entry) {
        try {
            updateFiles();

            long fileOffset = entryDataFile.appendEntry(entry);
            EntryIndexItem entryIndexItem = new EntryIndexItem(entry.getType(), entry.getIndex(), entry.getTerm(), fileOffset);
            boolean result = entryIndexFile.appendEntryIndexItem(entryIndexItem);

            if(result) {
                buffer.add(entry, entryIndexItem);
                lastLogIndex++;
                logger.debug("empty entry {} was appended", entry);
            }
            return result;
        } catch (IOException e) {
            logger.warn("fail to append empty entry to file, cause is: " + e.getMessage());
            //e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean appendEntry(Entry entry, long preTerm, long preLogIndex) {
        try {
            updateFiles();

            if(preLogIndex != entry.getIndex() - 1) {
                return false;
            }
            EntryIndexItem preEntryIndexItem = getPreEntryIndexItem(entry.getIndex());
            if(preEntryIndexItem == null ||  preEntryIndexItem.getTerm() != preTerm
                    || preEntryIndexItem.getIndex() != preLogIndex) {
                return false;
            }
            //发生冲突，删除后面的日志
            if(entry.getIndex() <= lastLogIndex) {
                logger.debug("entry conflict, start deleting entry from index {} ......", entry.getIndex());
                deleteLogEntriesFrom(entry.getIndex());
            }
            //添加到 data 文件
            long fileOffset = entryDataFile.appendEntry(entry);
            EntryIndexItem entryIndexItem = new EntryIndexItem(entry.getType(), entry.getIndex(), entry.getTerm(),
                    fileOffset);

            //添加到 index 文件
            boolean result =  entryIndexFile.appendEntryIndexItem(entryIndexItem);
            if(result) {
                logger.debug("entry {} was appended", entry);
                lastLogIndex++;
                //添加到buffer
                buffer.add(entry, entryIndexItem);
            }
            return result;
        } catch (IOException e) {
            //e.printStackTrace();
            logger.warn("fail to append entry, entry is {}, preTerm is {}, preLogIndex is {}, cause is: {}",
                    entry, preTerm, preLogIndex, e.getMessage());
            return false;
        }

    }


    /**
     * 获取entryIndex位置的前一个日志的 index 和 term，entryIndexFile和entryDataFile会跟随进行切换
     * @param entryIndex
     */
    private EntryIndexItem getPreEntryIndexItem(long entryIndex) throws IOException {
        if(buffer.contains(entryIndex)) {
            return buffer.getEntryItem(entryIndex);
        }
        EntryIndexItem indexItem = entryIndexFile.getPreEntryIndexItem(entryIndex);
        if(indexItem != null) {
            return indexItem;
        }
        //从后往前找
        int generationIndex = generationHandler.getCurrentGenerationIndex() - 1;
        while (generationIndex >= 0){
            EntryGeneration entryGeneration = new EntryGeneration(entryDataFile, entryIndexFile);
            EntryIndexFile indexFile = entryGeneration.getEntryIndexFile();
            indexItem = indexFile.getPreEntryIndexItem(entryIndex);
            if(indexItem != null) {
                entryDataFile = entryGeneration.getEntryDataFile();
                entryIndexFile = entryGeneration.getEntryIndexFile();
                return indexItem;
            }
            generationIndex--;
            entryGeneration.close();
        }
        //没找到
        return null;
    }

    /**
     * 如果文件大小超过限制，就创建新的文件
     * @throws IOException
     */
    private void updateFiles() throws IOException {
        long size = entryDataFile.getSize();
        if(size > maxFileSize * 1024) {
            logger.debug("current entry data file length is {} byte, greater than maxsize {}", size, maxFileSize);
            EntryIndexItem entryIndexItem = entryIndexFile.getLastEntryIndexItem();
            EntryGeneration entryGeneration = generationHandler
                    .createEntryGeneration(entryIndexItem.getIndex(), entryIndexItem.getTerm());
            entryIndexFile.close();
            entryDataFile.close();
            entryDataFile = entryGeneration.getEntryDataFile();
            entryIndexFile = entryGeneration.getEntryIndexFile();
        }
    }

    @Override
    public boolean deleteLogEntriesFrom(long logIndex) {
        if(logIndex > lastLogIndex) {
            return true;
        }
        try {
            //删buffer
            buffer.removeFrom(logIndex);
            //定位到具体的文件，从后往前找，边找边删
            EntryIndexItem entryIndexItem;
            do {
                entryIndexItem = entryIndexFile.getEntryIndexItem(logIndex);
                if(entryIndexItem == null) {
                    entryDataFile.close();
                    entryIndexFile.close();
                    generationHandler.deleteLatestGeneration();
                    EntryGeneration latestGeneration = generationHandler.getLatestGeneration();
                    entryDataFile = latestGeneration.getEntryDataFile();
                    entryIndexFile = latestGeneration.getEntryIndexFile();
                }
            } while (entryIndexItem == null);
            //定位到具体的文件后随后删除该文件后面的内容
            boolean res =  entryIndexFile.deleteEntriesFrom(entryIndexItem.getIndex())
                    && entryDataFile.deleteFromOffset(entryIndexItem.getOffset());
            if(res) {
                logger.debug("delete entries from index {}, latest entry data file is {}, entry index file is {}",
                        entryIndexItem.getIndex(), entryDataFile.getFilename(), entryIndexFile.getFilename());
                lastLogIndex = logIndex - 1;
            }
            return res;
        } catch (IOException e) {
            logger.warn("fail to delete entry from log index [{}], cause is: {}", logIndex, e.getMessage());
            return false;
        }
    }
    public void close() throws IOException {
        entryDataFile.close();
        entryIndexFile.close();
    }
    static class EntryBuffer {
        private static final Logger logger = LoggerFactory.getLogger(EntryBuffer.class);

        private final LinkedList<Entry> entryBuffer;
        private final LinkedList<EntryIndexItem> entryIndexItemBuffer;
        private final Map<Long, Entry> entryMap;
        private final Map<Long, EntryIndexItem> itemMap;
        private int currentSize;
        //buffer大小，单位为字节，默认1MB

        private final int BufferSize;

        public EntryBuffer(int bufferSize) {
            assert(bufferSize > 0);
            currentSize = 0;
            this.BufferSize = bufferSize;
            entryBuffer = new LinkedList<>();
            entryIndexItemBuffer = new LinkedList<>();
            entryMap = new HashMap<>();
            itemMap = new HashMap<>();
        }

        void add(Entry entry, EntryIndexItem indexItem) {
            int entrySize = entry.getSize();
            if(currentSize + entrySize > BufferSize) {
                if(entryBuffer.size() == 0) {
                    return;
                }
                logger.debug("buffer: current buffer size ({}) > buffer capacity ({}), remove the first entry: {}",
                        currentSize + entrySize, BufferSize, entryBuffer.getFirst());
                int size = entryBuffer.getFirst().getSize();
                currentSize = currentSize - size;
                removeFirst();
            }
            addLast(entry, indexItem);
            currentSize += entrySize;
            //while循环清除
            while (currentSize > BufferSize && entryBuffer.size() > 0) {
                currentSize -= entryBuffer.getFirst().getSize();
                entryBuffer.removeFirst();
            }
            logger.debug("buffer: {} was appended, current buffer size is {}, buffer capacity is {}", entry, currentSize, BufferSize);
        }
        Entry getEntry(long index) {
            Entry entry =  entryMap.get(index);
            if(entry != null) {
                logger.debug("buffer: entry index [{}] hit", index);
            }
            return entry;
        }
        EntryIndexItem getEntryItem(long index) {
            return itemMap.get(index);
        }
        void removeFrom(long index) {
//            if(!itemMap.containsKey(index)) {
//                return;
//            }
            while (entryBuffer.size() > 0 && entryBuffer.getLast().getIndex() >= index) {
                removeLast();
            }
        }
        boolean contains(long index) {
            if(entryBuffer.size() == 0) {
                return false;
            }
            long firstIndex = entryBuffer.getFirst().getIndex();
            long lastIndex = entryBuffer.getLast().getIndex();
            return firstIndex <= index && index <= lastIndex;
        }
        private void removeFirst() {
            EntryIndexItem indexItem =  entryIndexItemBuffer.removeFirst();
            entryBuffer.removeFirst();
            entryMap.remove(indexItem.getIndex());
            itemMap.remove(indexItem.getIndex());

        }
        int getEntrySize() {
            int size = entryBuffer.size();
            //assert(size == entryIndexItemBuffer.size() && size == entryMap.size() && size ==  itemMap.size());
            return entryBuffer.size();
        }
        private void removeLast() {
            long index = entryBuffer.getLast().getIndex();
            entryBuffer.removeLast();
            entryIndexItemBuffer.removeLast();
            entryMap.remove(index);
            itemMap.remove(index);

        }
        private void addLast(Entry entry, EntryIndexItem indexItem) {
            assert(entry.getIndex() == indexItem.getIndex());

            entryIndexItemBuffer.addLast(indexItem);
            entryBuffer.addLast(entry);
            entryMap.put(entry.getIndex(), entry);
            itemMap.put(indexItem.getIndex(), indexItem);
        }
    }
}
