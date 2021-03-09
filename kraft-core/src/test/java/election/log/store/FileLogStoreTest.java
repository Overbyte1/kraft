package election.log.store;


import election.log.entry.EmptyEntry;
import election.log.entry.Entry;
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

    @Test
    public void testGetLogEntry() {
        File file = new File("abc/cde");
        file.mkdirs();
    }

    public void testAppendEmptyEntry() {
    }
    @Test
    public void testAppendEntry() {
        Entry entry1 = new EmptyEntry(1, 1);
        fileLogStore.appendEmptyEntry(entry1);
        Entry entry2 = new EmptyEntry(1, 2);
        boolean b = fileLogStore.appendEntry(entry1, 1, 0);

        assert b == true;

        Entry entry3 = fileLogStore.getLastEntry();
        assert entry1.equals(entry3);
        //TODO:测试其余方法
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