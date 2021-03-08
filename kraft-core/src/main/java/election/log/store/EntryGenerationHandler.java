package election.log.store;

import java.io.*;

/**
 * 为避免所有entry都存在一个文件中导致文件过大，所以将entry保存到不同文件，因为一个entry文件会搭配一个entryIndex文件，
 * 所以将这两个文件的创建、删除是一起的，因此将它们合并成一个Generation
 */
public class EntryGenerationHandler {
    private String rootPath = "entry";
    private static final String commonPrefix = "entry_";
    private static final String entryDataFilenameSuffix = ".bin";
    private static final String entryIndexFileNameSuffix = ".idx";

    private static final int MAX_GENERATION = 4096;

    private int currentIndex = -1;

    private long lastGenerationEntryIndex;


    public EntryGenerationHandler(String path) {
        rootPath = path;
    }

    //创建新的Generation
    public EntryGeneration createEntryGeneration(long preIndex, long preTerm) throws IOException {
        if(currentIndex < 0) {
            initGeneration();
        }
        currentIndex++;
        String entryDataName = commonPrefix + currentIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentIndex + entryIndexFileNameSuffix;
        //创建文件
        File entryDatafile = new File(entryDataName);
        if(!entryDatafile.exists()) {
            entryDatafile.createNewFile();
        }
        File entryIndexFile  = new File(entryIndexName);
        if(!entryIndexFile.exists()) {
            entryIndexFile.createNewFile();
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(entryIndexName, "r");
        //TODO:change 8
        randomAccessFile.seek(randomAccessFile.length() - 8);
        lastGenerationEntryIndex = randomAccessFile.readLong();
        return new EntryGeneration(new EntryDataFile(entryIndexFile),
                                    new EntryIndexFile(entryIndexFile, preIndex, preTerm));
    }
    private void initGeneration() throws IOException {
        File file;
        int low = 0, high = MAX_GENERATION, mid;
        String fileName = null;
        //二分查找
        while(low < high) {
            mid = low + (high - low) / 2;
            fileName = commonPrefix + mid + entryDataFilenameSuffix;
            file = new File(fileName);
            if(file.exists()) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        currentIndex = low;
        String entryDataName = commonPrefix + currentIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentIndex + entryIndexFileNameSuffix;
        file = new File(entryDataName);
        if(!file.exists()) {
            file.createNewFile();
        }
        file = new File(entryIndexName);
        if(!file.exists()) {
            file.createNewFile();
        }
        if(currentIndex == 0) {
            lastGenerationEntryIndex = 0;
        } else { //获取上一个Generation的最后一个entryIndex
            RandomAccessFile randomAccessFile = new RandomAccessFile(entryIndexName, "r");
            //TODO:change 8
            randomAccessFile.seek(randomAccessFile.length() - 8);
            lastGenerationEntryIndex = randomAccessFile.readLong();
        }
    }


    //获取最新的Generation
    public EntryGeneration getLatestGeneration() throws IOException {
        if(currentIndex < 0) {
            initGeneration();
        }
        String entryDataName = commonPrefix + currentIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentIndex + entryIndexFileNameSuffix;
        EntryDataFile entryDataFile;
        EntryIndexFile entryIndexFile;
        entryDataFile = new EntryDataFile(new File(entryDataName));
        //如果currentIndex等于0说明这是创建的第一个文件
        if(currentIndex == 0) {
            entryIndexFile = new EntryIndexFile(new File(entryIndexName), 0, 0);
        } else {
            entryIndexFile = new EntryIndexFile(new File(entryIndexName));
        }
        return new EntryGeneration(entryDataFile, entryIndexFile);
    }



}
