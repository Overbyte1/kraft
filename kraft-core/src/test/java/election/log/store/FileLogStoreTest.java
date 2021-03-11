package election.log.store;


import election.log.entry.EmptyEntry;
import election.log.entry.Entry;
import election.log.entry.GeneralEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class FileLogStoreTest {
    private FileLogStore fileLogStore;
    private String path = "./data/";
    @Before
    public void init() {
        try {
            fileLogStore = new FileLogStore(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void deleteAllFiles() {
        File file = new File(path);
        deleteFile(file);
    }
    private void deleteFile(File file) {
        if(file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File file1 : files) {
            deleteFile(file1);
        }
    }

    @Test
    public void testGetLogEntry() {
        testAppendEntry();
        Entry logEntry = fileLogStore.getLogEntry(100);
        assertEquals(100, logEntry.getIndex());
    }

    @Test
    public void testAppendEmptyEntry() {
        Entry entry1 = new EmptyEntry(1, 1);
        boolean res = fileLogStore.appendEmptyEntry(entry1);
        assertEquals(true, res);
        Entry entry = fileLogStore.getLogEntry(entry1.getIndex());
        assertEquals(entry1, entry);

        Entry entry2 = new EmptyEntry(1, 1);


    }
    @Test
    public void testAppendEntry() {

        Entry entry1 = new EmptyEntry(1, 1);
        boolean appendEmptyEntry = fileLogStore.appendEmptyEntry(entry1);
        assertEquals(true, appendEmptyEntry);

        Entry entry2 = new GeneralEntry(2, 2, new byte[]{1,3});
        boolean appendEntry = fileLogStore.appendEntry(entry2, 1, 2);
        assertEquals(false, appendEntry);

        Entry lastEntry = fileLogStore.getLastEntry();
        assertEquals(entry1, lastEntry);

        Entry entry3 = new GeneralEntry(2, 2, new byte[]{9,2});
        boolean appendEntry1 = fileLogStore.appendEntry(entry3, 1, 1);
        assertEquals(true, appendEntry1);

        assertEquals(entry3, fileLogStore.getLastEntry());

        long preTerm = 2, preIndex = 2;
        byte[] bytes = new byte[]{-1};
        for(int i = 0; i < 2000; i++) {
            Entry generalEntry = new GeneralEntry(preTerm, preIndex + 1, bytes);
            assertEquals(true, fileLogStore.appendEntry(generalEntry, preTerm, preIndex));
            assertEquals(generalEntry, fileLogStore.getLogEntry(generalEntry.getIndex()));
            preIndex = generalEntry.getIndex();
        }
    }

    @Test
    public void testDeleteLogEntriesFrom() {
        testAppendEntry();
        long deleteIndex = 1998;
        boolean b = fileLogStore.deleteLogEntriesFrom(deleteIndex);
        assertEquals(true, b);
        long index = fileLogStore.getLastEntry().getIndex();
        assertEquals(deleteIndex - 1, index);

        deleteIndex = 1500;
        b = fileLogStore.deleteLogEntriesFrom(deleteIndex);
        assertEquals(true, b);
        index = fileLogStore.getLastEntry().getIndex();
        assertEquals(deleteIndex - 1, index);

    }
    @After
    public void destroy() {
        try {
            fileLogStore.close();
            deleteAllFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}