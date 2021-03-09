package election.log.store;

import java.io.IOException;

public class EntryGeneration implements AutoCloseable {
//    private String entryDataName;
//    private String entryIndexName;

    private EntryDataFile entryDataFile;
    private EntryIndexFile entryIndexFile;

    public EntryGeneration(EntryDataFile entryDataFile, EntryIndexFile entryIndexFile) {
        this.entryDataFile = entryDataFile;
        this.entryIndexFile = entryIndexFile;
    }

    public EntryDataFile getEntryDataFile() {
        return entryDataFile;
    }

    public void setEntryDataFile(EntryDataFile entryDataFile) {
        this.entryDataFile = entryDataFile;
    }

    public EntryIndexFile getEntryIndexFile() {
        return entryIndexFile;
    }

    public void setEntryIndexFile(EntryIndexFile entryIndexFile) {
        this.entryIndexFile = entryIndexFile;
    }

    public void close() {
        try {
            if(entryDataFile != null) {
                entryDataFile.close();
            }
            if(entryIndexFile != null) {
                entryIndexFile.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
