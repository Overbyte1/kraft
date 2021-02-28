package election.log;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO:保证线程安全
public class MemoryLogStore implements LogStore {
    private List<Entry> entryList;
    private int lastLogIndex;
    private int offset;

    public MemoryLogStore() {
        entryList = Collections.synchronizedList(new ArrayList<>());
        lastLogIndex = 0;
        offset = 0;
    }

    @Override
    public Entry getLogEntry(long logIndex) {
        return entryList.get(toListIndex(logIndex));
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
        if(logIndex == 0) {
            return new ArrayList<>();
        }
        int fromIndex = toListIndex(logIndex);
        int endIndex = entryList.size();
        List<Entry> list = new ArrayList<>(endIndex - fromIndex + 1);
        while(fromIndex <= endIndex) {
            list.add(entryList.get(fromIndex));
            fromIndex++;
        }
        return list;
    }

    /**
     * 附加日志，需要保证线程安全
     * @param preTerm
     * @param preLogIndex
     * @param logs
     * @return
     */
    @Override
    public synchronized boolean appendEntries(long preTerm, long preLogIndex, List<Entry> logs) {
        //心跳日志
        if(logs.size() == 0) {
            return true;
        }
        long index = logs.get(0).getIndex();
        //判断index位置处的preTerm与preLogIndex是否匹配
        if(match(index, preTerm, preLogIndex)) {
            boolean res = entryList.addAll(logs);
            if(res) {
                lastLogIndex += logs.size();
            }
            return res;
        }
        return false;
    }

    @Override
    public synchronized void appendEntry(Entry entry) {
        entry.setIndex(lastLogIndex);
        entryList.add(entry);
        lastLogIndex++;
    }

    @Override
    public synchronized boolean deleteLogEntriesFrom(long logIndex) {
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
