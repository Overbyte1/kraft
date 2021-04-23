package log.store;

import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.GeneralEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class FileLogStoreTest {

    @Test
    public void testBufferAdd() {
        FileLogStore.EntryBuffer buffer = new FileLogStore.EntryBuffer(1024);
        Entry entry = new EmptyEntry(3, 1);
        EntryIndexItem item = new EntryIndexItem(0, 1, 1, 1);
        buffer.add(entry, item);
        assertEquals(entry, buffer.getEntry(entry.getIndex()));
        assertEquals(item, buffer.getEntryItem(entry.getIndex()));

        for(int i = 0; i < 1000; i++) {
            entry = new EmptyEntry(0, i);
            item = new EntryIndexItem(0, i, 0, 9);
            buffer.add(entry, item);
        }
        assertEquals(null, buffer.getEntry(1));
        try {
            entry.setIndex(2);
            item.setIndex(3);
            buffer.add(entry, item);
            assertEquals(true, false);
        } catch (Error e) {
            assertEquals(true, true);
        }

    }
    @Test
    public void testBufferRemoveFrom() {
        FileLogStore.EntryBuffer buffer = new FileLogStore.EntryBuffer(10240);
        Entry entry = new EmptyEntry(1, 1);
        EntryIndexItem indexItem = new EntryIndexItem(1, 1, 1, 0);
        buffer.add(entry, indexItem);
        buffer.removeFrom(0);
        assertEquals(null, buffer.getEntry(1));
        assertEquals(null, buffer.getEntryItem(1));

        int index = 100;

        for(int i = 1; i < index; i++) {
            entry = new EmptyEntry(1, i);
            indexItem = new EntryIndexItem(1, i, 1, 0);
            buffer.add(entry, indexItem);
            assertEquals(i, buffer.getEntrySize());
        }
        buffer.removeFrom(index);
        assertEquals(index - 1, buffer.getEntry(index - 1).getIndex());
        buffer.removeFrom(50);
        assertEquals(49, buffer.getEntry(49).getIndex());
        assertEquals(49, buffer.getEntryItem(49).getIndex());
        assertEquals(null, buffer.getEntryItem(50));
        assertEquals(null, buffer.getEntryItem(50));

        buffer.removeFrom(1);
        assertEquals(null, buffer.getEntryItem(1));
        assertEquals(0, buffer.getEntrySize());

    }

    @Test
    public void getLogEntry() throws IOException {
        String path = "./data/logstoreGetLogEntry/";
        File file = new File(path);
        deleteFiles(file);

        FileLogStore fileLogStore = new FileLogStore(path, 1024, 1);
        Entry entry;
        for(int i = 0; i < 1000; i++) {
            entry = new GeneralEntry(0, i, new byte[]{(byte)i});
            fileLogStore.appendEntry(entry, 0, i - 1);
        }
        assertEquals(99, fileLogStore.getLogEntry(99).getIndex());
        assertEquals(1, fileLogStore.getLogEntry(1).getIndex());
        assertEquals(999, fileLogStore.getLogEntry(999).getIndex());
        assertEquals(null, fileLogStore.getLogEntry(1000));

        fileLogStore.close();

        deleteFiles(file);
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
    public void deleteLogEntriesFrom() throws IOException {
        String path = "./data/logstoreDeleteEntryFrom/";
        File file = new File(path);
        deleteFiles(file);

        FileLogStore fileLogStore = new FileLogStore(path, 1024, 1);
        Entry entry;
        for(int i = 0; i < 1000; i++) {
            entry = new GeneralEntry(0, i, new byte[]{(byte)i});
            fileLogStore.appendEntry(entry, 0, i - 1);
        }
        assertEquals(999, fileLogStore.getLogEntry(999).getIndex());
        fileLogStore.deleteLogEntriesFrom(999);
        assertEquals(null, fileLogStore.getLogEntry(999));

        fileLogStore.deleteLogEntriesFrom(100);
        assertEquals(null, fileLogStore.getLogEntry(100));
        assertEquals(99, fileLogStore.getLogEntry(99).getIndex());

        fileLogStore.deleteLogEntriesFrom(1);
        assertEquals(null, fileLogStore.getLogEntry(1));

        for(int i = 0; i < 200; i++) {
            entry = new GeneralEntry(0, i, new byte[]{(byte)i});
            fileLogStore.appendEntry(entry, 0, i - 1);
        }
        assertEquals(199, fileLogStore.getLogEntry(199).getIndex());
        assertEquals(null, fileLogStore.getLogEntry(200));
        assertEquals(1, fileLogStore.getLogEntry(1).getIndex());

        fileLogStore.close();

        deleteFiles(file);
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
        if(files == null || files.length == 0) {
            file.delete();
        }
        for (File file1 : files) {
            deleteFiles(file1);
        }
        file.delete();
    }
}
