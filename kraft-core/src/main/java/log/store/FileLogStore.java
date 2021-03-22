package log.store;

import log.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件进行存储，需要考虑：
 * 1. 文件的存储格式、位置、命名
 * 2. 写回文件，读取文件的时机
 *
 * TODO：将所有的Generation进行抽象，让FileLogStore好像只在操作一个Generation
 */
public class FileLogStore extends AbstractLogStore implements LogStore {
    private static final Logger logger = LoggerFactory.getLogger(FileLogStore.class);

    private EntryDataFile entryDataFile;
    private EntryIndexFile entryIndexFile;
    private EntryGenerationHandler generationHandler;

    //TODO:参数配置，buffer容量
    private final int BUFFER_SIZE = 1024;

    //TODO:配置成参数，默认2G
    private int maxFileSize = 10 * 1024;

    private List<Entry> entryBuffer;
    private List<EntryIndexItem> entryIndexItemBuffer;


    public FileLogStore(String path) throws IOException {
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

        entryBuffer = new ArrayList<>(BUFFER_SIZE);
        entryIndexItemBuffer = new ArrayList<>(BUFFER_SIZE);
        //TODO：完善buffer
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
        //先找bufferList，之后找当前Generation，否则进行二分查找
        if(logIndex > lastLogIndex) {
            return null;
        }
        if(!entryBuffer.isEmpty() && logIndex >= entryBuffer.get(0).getIndex()) {
            long startLogIndex = entryBuffer.get(0).getIndex();
            return entryBuffer.get((int)(logIndex - startLogIndex));
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

            //addToBuffer(entry, entryIndexItem);

            lastLogIndex++;
            logger.debug("empty entry {} was appended", entry);
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
                //addToBuffer(entry, entryIndexItem);
                return true;
            }
            return false;
        } catch (IOException e) {
            //e.printStackTrace();
            logger.warn("fail to append entry, entry is {}, preTerm is {}, preLogIndex is {}, cause is: {}",
                    entry, preTerm, preLogIndex, e.getMessage());
            return false;
        }

    }
    private void addToBuffer(Entry entry, EntryIndexItem entryIndexItem) {
        if(entryBuffer.size() > BUFFER_SIZE) {
            entryBuffer.clear();
            entryIndexItemBuffer.clear();
            //TODO:会引起命中率大幅下降，建议渐进式清除
        }
        entryBuffer.add(entry);
        entryIndexItemBuffer.add(entryIndexItem);
    }
    private Entry getEntryFromBuffer(long entryIndex) {
        if(!entryBuffer.isEmpty() && entryIndex >= entryBuffer.get(0).getIndex()) {
            long startLogIndex = entryBuffer.get(0).getIndex();
            return entryBuffer.get((int)(entryIndex - startLogIndex));
        }
        return null;
    }

    /**
     * 获取entryIndex位置的前一个日志的 index 和 term，entryIndexFile和entryDataFile会跟随进行切换
     * @param entryIndex
     */
    private EntryIndexItem getPreEntryIndexItem(long entryIndex) throws IOException {
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
            if(indexFile != null) {
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
     * 如果文件大小超过限制，就创建新的
     * @throws IOException
     */
    private void updateFiles() throws IOException {
        long size = entryDataFile.getSize();
        if(size > maxFileSize) {
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
        try {
            //首先查看是否位于 bufferList中
            if(!entryBuffer.isEmpty() && logIndex >= entryBuffer.get(0).getIndex()) {
                int idx = entryBuffer.size() - 1;
                while(entryBuffer.get(idx).getIndex() >= logIndex) {
                    entryBuffer.remove(idx);
                }
                lastLogIndex = logIndex - 1;
                return true;
            }
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
            e.printStackTrace();
            return false;
        }
    }
    public void close() throws IOException {
        entryDataFile.close();
        entryIndexFile.close();
    }
}
