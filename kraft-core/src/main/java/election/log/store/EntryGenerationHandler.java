package election.log.store;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 为避免所有entry都存在一个文件中导致文件过大，所以将entry保存到不同文件，因为一个entry文件会搭配一个entryIndex文件，
 * 所以将这两个文件的创建、删除是一起的，因此将它们合并成一个Generation
 */
public class EntryGenerationHandler {
    private String rootPath = "entry";
    private static final String commonPrefix = "entry_";
    private static final String entryDataFilenameSuffix = ".bin";
    private static final String entryIndexFileNameSuffix = ".idx";
    private final StringBuilder stringBuilder = new StringBuilder();

    private static final int MAX_GENERATION = 4096;

    private int currentGenerationIndex = -1;

    private long lastGenerationEntryIndex;


    public EntryGenerationHandler(String path) {
        rootPath = path;
    }

    //创建新的Generation
    public EntryGeneration createEntryGeneration(long preIndex, long preTerm) throws IOException {
        if(currentGenerationIndex < 0) {
            initGeneration();
        }
        currentGenerationIndex++;
        String entryDataName = commonPrefix + currentGenerationIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentGenerationIndex + entryIndexFileNameSuffix;
        //创建文件
        File entryDatafile = new File(entryDataName);
        if(!entryDatafile.exists()) {
            entryDatafile.createNewFile();
        }
        File entryIndexFile  = new File(entryIndexName);
        if(!entryIndexFile.exists()) {
            entryIndexFile.createNewFile();
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(entryIndexName, "r");){
            //TODO:change 8
            randomAccessFile.seek(randomAccessFile.length() - 8);
            lastGenerationEntryIndex = randomAccessFile.readLong();
            return new EntryGeneration(new EntryDataFile(entryIndexFile),
                    new EntryIndexFile(entryIndexFile, preIndex, preTerm));
        }
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
        currentGenerationIndex = low;
        String entryDataName = commonPrefix + currentGenerationIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentGenerationIndex + entryIndexFileNameSuffix;
        file = new File(entryDataName);
        if(!file.exists()) {
            file.createNewFile();
        }
        file = new File(entryIndexName);
        if(!file.exists()) {
            file.createNewFile();
        }
        if(currentGenerationIndex == 0) {
            lastGenerationEntryIndex = 0;
        } else { //获取上一个Generation的最后一个entryIndex
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(entryIndexName, "r")) {

                //TODO:change 8
                randomAccessFile.seek(randomAccessFile.length() - 8);
                lastGenerationEntryIndex = randomAccessFile.readLong();
            }
        }
    }


    //获取最新的Generation
    public EntryGeneration getLatestGeneration() throws IOException {
        if(currentGenerationIndex < 0) {
            initGeneration();
        }
        String entryDataName = getEntryDataFilename();
        String entryIndexName = getEntryIndexFileName();

        EntryDataFile entryDataFile;
        EntryIndexFile entryIndexFile;
        entryDataFile = new EntryDataFile(new File(entryDataName));
        //如果currentGenerationIndex等于0说明这是创建的第一个文件
        if(currentGenerationIndex == 0) {
            entryIndexFile = new EntryIndexFile(new File(entryIndexName), 0, 0);
        } else {
            entryIndexFile = new EntryIndexFile(new File(entryIndexName));
        }
        return new EntryGeneration(entryDataFile, entryIndexFile);
    }
    
    public EntryGeneration getGeneration(int generation) throws IOException {
        if(generation > currentGenerationIndex) {
            return null;
        }
        String entryDataName = getEntryDataFilename();
        String entryIndexName = getEntryIndexFileName();

        EntryDataFile entryDataFile = new EntryDataFile(new File(entryDataName));
        EntryIndexFile entryIndexFile = new EntryIndexFile(new File(entryIndexName));

        return new EntryGeneration(entryDataFile, entryIndexFile);
    }

    public int getCurrentGenerationIndex() {
        return currentGenerationIndex;
    }
    
    
    private String getFilename(String suffix) {
        if(stringBuilder.length() != 0) {
            stringBuilder.delete(0, stringBuilder.length());
        }
        stringBuilder.append(rootPath)
                .append(commonPrefix)
                .append(currentGenerationIndex)
                .append(suffix);
        return stringBuilder.toString();
    }
    private String getEntryIndexFileName() {
        return getFilename(entryIndexFileNameSuffix);
    }
    private String getEntryDataFilename() {
        return getFilename(entryDataFilenameSuffix);
    }

    /**
     * 删除最近的Generation
     * @return
     */
    public boolean deleteLatestGeneration() {
        String entryDataName = commonPrefix + currentGenerationIndex + entryDataFilenameSuffix;
        String entryIndexName = commonPrefix + currentGenerationIndex + entryIndexFileNameSuffix;
        File entryDataFile = new File(entryDataName);
        File entryIndexFile = new File(entryIndexName);
        if(entryDataFile.delete() && entryIndexFile.delete()) {
            currentGenerationIndex--;
            return true;
        }
        return false;

    }



}
