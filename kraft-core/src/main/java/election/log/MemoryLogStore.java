package election.log;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;

import java.util.List;

public class MemoryLogStore implements LogStore {
    private List<Entry> entryList;
    private int lastLogIndex;
    private int offset = 0;

    @Override
    public Entry getLogEntry(long logIndex) {
        return entryList.get((int) (logIndex - offset));
    }

    @Override
    public Entry getLastEntry() {
        int listIndex = getListIndex(lastLogIndex);
        return entryList.get(listIndex);
    }

    @Override
    public EntryMeta getEntryMata(long logIndex) {
        Entry lastEntry = getLastEntry();
        return new EntryMeta(lastEntry.getIndex(), lastEntry.getTerm());
    }

    @Override
    public List<Entry> getLogEntriesFrom(long logIndex) {
        int fromIndex = getListIndex(logIndex);
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
    public boolean deleteLogEntriesFrom(long logIndex) {
        int fromIndex = getListIndex(logIndex);
        for(int i = entryList.size(); i >= fromIndex; i--) {
            entryList.remove(i);
        }
        return true;
    }

    @Override
    public boolean match(long logIndex, long preTerm, long preLogIndex) {

        EntryMeta entryMata = getEntryMata(logIndex - 1);
        return entryMata.getPreLogIndex() == preLogIndex && entryMata.getTerm() == preTerm;
    }

    @Override
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    private int getListIndex(long logIndex) {
        return (int) (logIndex - offset);
    }

}
