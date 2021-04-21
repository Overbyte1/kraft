package log.store;

import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.GeneralEntry;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileLogStoreTest {

    @Test
    public void getLogEntry() {
    }

    @Test
    public void appendEmptyEntry() throws IOException {
        String path = "./data/logstore/";
        File file = new File(path);
        deleteFiles(file);

        FileLogStore fileLogStore = new FileLogStore(path);

        EmptyEntry entry = new EmptyEntry(44, 1);
        boolean b = fileLogStore.appendEmptyEntry(entry);
        assertEquals(true, b);
        assertEquals(true, file.exists());
        Entry logEntry = fileLogStore.getLogEntry(1);
        assertEquals(entry, logEntry);

        logEntry =  fileLogStore.getLogEntry(2);
        assertEquals(null, logEntry);

        logEntry =  fileLogStore.getLogEntry(0);
        assertEquals(null, logEntry);

        fileLogStore.close();

        deleteFiles(file);
    }

    @Test
    public void appendEntry() throws IOException {
        String path = "./data/logstore1/";
        File file = new File(path);
        deleteFiles(file);

        Entry entry = new GeneralEntry(3, 1, new byte[]{1, 4, 3, 2});

        FileLogStore fileLogStore = new FileLogStore(path, 1, 1024);
        boolean b = fileLogStore.appendEntry(entry, 0, 0);
        assertEquals(true, b);
        assertEquals(entry, fileLogStore.getLogEntry(entry.getIndex()));

        entry.setIndex(2);
        b = fileLogStore.appendEntry(entry, 0, 0);
        assertEquals(false, b);

        assertEquals(null, fileLogStore.getLogEntry(2));
        assertEquals(null, fileLogStore.getLogEntry(0));


        deleteFiles(file);

    }

    @Test
    public void deleteLogEntriesFrom() {
    }

    private void deleteFiles(File file) {
        if(!file.exists()) {
            return;
        }
        if(file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File file1 : files) {
            deleteFiles(file1);
        }
        file.delete();
    }
}