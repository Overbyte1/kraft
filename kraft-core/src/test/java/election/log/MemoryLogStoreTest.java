package election.log;

import election.LogIndexOutOfBoundsException;
import election.log.entry.EmptyEntry;
import election.log.entry.Entry;
import election.log.entry.EntryMeta;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class MemoryLogStoreTest extends TestCase {
    private MemoryLogStore log = new MemoryLogStore();

    public void testGetLogEntry() {
        assertEquals(true, log.isEmpty());

        Entry entry = new EmptyEntry(0, 1);
        log.appendEntry(entry);

        assertEquals(1, log.size());

        try {
            log.getLogEntry(0);
        } catch (LogIndexOutOfBoundsException e) {
            Entry logEntry = log.getLogEntry(1);
            assertEquals(entry, logEntry);;
            return;
        }
        assertEquals(true, false);
    }

    public void testGetLastEntry() {
        Entry entry = new EmptyEntry(0, 1);
        try {
            log.getLastEntry();
        } catch (LogIndexOutOfBoundsException e) {
            log.appendEntry(entry);
            assertEquals(entry, log.getLastEntry());
            return;
        }
        assertEquals(true, false);
    }

    public void testGetEntryMata() {
        Entry entry = new EmptyEntry(0, 1);
        Entry entry1 = new EmptyEntry(4, 4);
        try {
            log.getEntryMata(0);
        } catch (LogIndexOutOfBoundsException e) {
            log.appendEntry(entry);
            log.appendEntry(entry1);
            EntryMeta entryMeta = log.getEntryMata(log.getLastLogIndex());
            assertEquals(entry1.getIndex(), entryMeta.getLogIndex());
            assertEquals(entry1.getTerm(), entryMeta.getTerm());
            assertEquals(log.getLastLogIndex(), entry1.getIndex());
            //assertEquals(entry.getType(), entryMeta.get());
            return;
        }
        assertEquals(true, false);
    }

    public void testGetLogEntriesFrom() {
        Entry entry = new EmptyEntry(0, 1);
        Entry entry1 = new EmptyEntry(4, 4);
        try {
            log.getLogEntriesFrom(0);
        } catch (LogIndexOutOfBoundsException e) {
            log.appendEntry(entry);
            log.appendEntry(entry1);
            List<Entry> entries = log.getLogEntriesFrom(1);
            for (Entry entry2 : entries) {
                assertEquals(entry2, log.getLogEntry(entry2.getIndex()));
            }
            //assertEquals(entry.getType(), entryMeta.get());
            return;
        }
        assertEquals(true, false);
    }

    public void testAppendEntries() {
        List<Entry> list = new ArrayList<>();
        Entry entry = new EmptyEntry(0, 1);
        Entry entry1 = new EmptyEntry(4, 2);
        Entry entry2 = new EmptyEntry(4, 3);
        list.add(entry);
        list.add(entry1);

        List<Entry> entryList = new ArrayList<>();
        entryList.add(entry2);

        boolean b = log.appendEntries(0, 0, list);
        assertEquals(true, b);
        List<Entry> logEntriesFrom = log.getLogEntriesFrom(1);
        assertEquals(list.size(), logEntriesFrom.size());;
        for(int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), logEntriesFrom.get(i));
        }
        boolean b1 = log.appendEntries(3, 2, entryList);
        assertEquals(false, b1);
        b1 = log.appendEntries(4, 3, entryList);
        assertEquals(false, b1);
        b1 = log.appendEntries(4, 2, entryList);
        assertEquals(true, b1);


    }

    public void testAppendEntry() {

    }

    public void testDeleteLogEntriesFrom() {
        Entry[] entries = new Entry[10];
        List<Entry> list = new ArrayList<>();
        for(int i = 0; i < entries.length; i++) {
            entries[i] = new EmptyEntry(1, i + 1);
            list.add(entries[i]);
        }
        log.appendEntries(0, 0, list);
        assertEquals(entries.length, log.getLastLogIndex());
        log.deleteLogEntriesFrom(1);
        assertEquals(0, log.getLastLogIndex());

        log.appendEntries(0, 0, list);
        assertEquals(entries.length, log.getLastLogIndex());

        log.appendEntry(new EmptyEntry(0, 0));
        assertEquals(11, log.getLastLogIndex());
    }

    public void testMatch() {
    }

    public void testGetLastLogIndex() {
    }
}