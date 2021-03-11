package election.log.store;


import election.log.Log;
import election.log.entry.EmptyEntry;
import election.log.entry.Entry;
import election.log.entry.GeneralEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

public class FileLogStoreTest {
    private FileLogStore fileLogStore;
    @Before
    public void init() {
        try {
            fileLogStore = new FileLogStore("./data/");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void deleteAllFiles() {

    }

    @Test
    public void testGetLogEntry() {

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

    public void testDeleteLogEntriesFrom() {

    }
    @After
    public void destroy() {
        try {
            fileLogStore.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}