package election.log;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;

import java.util.ArrayList;
import java.util.List;

//TODO:保证线程安全
public class MemoryLogStore implements LogStore {
    private List<Entry> entryList;
    private int lastLogIndex;
    private int offset;

    public MemoryLogStore() {
        entryList = new ArrayList<>();
        lastLogIndex = 0;
        offset = 0;
    }

    @Override
    public Entry getLogEntry(long logIndex) {
        return entryList.get((int) (logIndex - offset));
    }

    @Override
    public Entry getLastEntry() {
        int listIndex = toListIndex(lastLogIndex);
        return entryList.get(listIndex);
    }

    @Override
    public EntryMeta getEntryMata(long logIndex) {
        Entry lastEntry = getLastEntry();
        return new EntryMeta(lastEntry.getIndex(), lastEntry.getTerm());
    }

    @Override
    public List<Entry> getLogEntriesFrom(long logIndex) {
        int fromIndex = toListIndex(logIndex);
        return entryList.subList(fromIndex, entryList.size());
    }

    @Override
    public boolean appendEntries(List<Entry> logs) {
        boolean res = entryList.addAll(logs);
        if(res) {
            lastLogIndex += logs.size();
        }
        return res;
    }

    @Override
    public void appendEntry(Entry entry) {
        entryList.add(entry);
    }

    @Override
    public boolean deleteLogEntriesFrom(long logIndex) {
        int fromIndex = toListIndex(logIndex);
        for(int i = entryList.size(); i >= fromIndex; i--) {
            entryList.remove(i);
        }
        return true;
    }

    @Override
    public boolean match(long logIndex, long preTerm, long preLogIndex) {

        EntryMeta entryMata = getEntryMata(logIndex - 1);
        return entryMata.getLogIndex() == preLogIndex && entryMata.getTerm() == preTerm;
    }

    @Override
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    private int toListIndex(long logIndex) {
        return (int) (logIndex - offset);
    }

    private long toLogIndex(int listIndex) {
        return listIndex + offset;
    }

}
