package election.log.store;

import election.log.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件进行存储，需要考虑：
 * 1. 文件的存储格式、位置、命名
 * 2. 写回文件，读取文件的时机
 */
public class FileLogStore extends AbstractLogStore implements LogStore {
    private static final Logger logger = LoggerFactory.getLogger(FileLogStore.class);

    private EntryDataFile entryDataFile;
    private EntryIndexFile entryIndexFile;
    private EntryGenerationHandler generationHandler;

    //TODO:配置成参数，默认2G
    private int maxFileSize = 2 * 1024 * 1024;

    private List<Entry> bufferList = new ArrayList<>();

    public FileLogStore(String path) throws IOException {
        generationHandler = new EntryGenerationHandler(path);
        EntryGeneration latestGeneration = generationHandler.getLatestGeneration();
        entryDataFile = latestGeneration.getEntryDataFile();
        entryIndexFile = latestGeneration.getEntryIndexFile();
    }

    @Override
    public Entry getLogEntry(long logIndex) {
        //先找bufferList，之后找当前Generation，否则进行二分查找
        return null;
    }

    @Override
    public boolean appendEmptyEntry(Entry entry) {
        try {
            updateFiles();

            long fileOffset = entryDataFile.appendEntry(entry);
            EntryIndexItem entryIndexItem = new EntryIndexItem(entry.getType(), entry.getIndex(), entry.getTerm(), fileOffset);
            entryIndexFile.appendEntryIndexItem(entryIndexItem);
            return true;
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

            long fileOffset = entryDataFile.appendEntry(entry);
            EntryIndexItem entryIndexItem = new EntryIndexItem(entry.getType(), entry.getIndex(), entry.getTerm(),
                    fileOffset);
            return entryIndexFile.appendEntryIndexItem(entryIndexItem);

        } catch (IOException e) {
            //e.printStackTrace();
            logger.warn("fail to append entry, entry is: {}, preTerm is {}, preLogIndex is {}, cause is: ",
                    entry, preTerm, preLogIndex, e.getMessage());
            return false;
        }

    }
    private void updateFiles() throws IOException {
        if(entryDataFile.getSize() > maxFileSize) {
            EntryIndexItem entryIndexItem = entryIndexFile.getLastEntryIndexItem();
            EntryGeneration entryGeneration = generationHandler
                    .createEntryGeneration(entryIndexItem.getIndex(), entryIndexItem.getTerm());
            entryDataFile = entryGeneration.getEntryDataFile();
            entryIndexFile = entryGeneration.getEntryIndexFile();
        }
    }

    @Override
    public boolean deleteLogEntriesFrom(long logIndex) {
        return false;
    }
}
