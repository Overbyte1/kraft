package log.store;

import election.LogIndexOutOfBoundsException;
import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.EntryMeta;
import log.entry.GeneralEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLogStore implements LogStore{
    private static final Logger logger = LoggerFactory.getLogger(AbstractLogStore.class);

    protected long lastLogIndex;

    public AbstractLogStore() {
        EmptyEntry.registerSerializer();
        GeneralEntry.registerSerializer();
    }

    @Override
    public Entry getLastEntry() {
        if(lastLogIndex < 1) {
            throw new LogIndexOutOfBoundsException("last log index is" + lastLogIndex + ", no any log");
        }
        return getLogEntry(lastLogIndex);
    }

    @Override
    public long getLastLogIndex() {
        return lastLogIndex;
    }

    @Override
    public EntryMeta getEntryMata(long logIndex) {
        if(lastLogIndex < 1 || logIndex > lastLogIndex || logIndex < 1) {
            throw new LogIndexOutOfBoundsException("" + logIndex);
        }
        Entry entry = getLogEntry(logIndex);
        return new EntryMeta(entry.getIndex(), entry.getTerm());
    }

    @Override
    public EntryMeta getPreEntryMeta(long logIndex) {
        //索引为0的 term=0, logIndex=0，但只能在进行匹配时使用
        if(logIndex == 1) {
            return new EntryMeta(0, 0);
        }
        return getEntryMata(logIndex - 1);
    }

    @Override
    public List<Entry> getLogEntriesFrom(long logIndex) {
        if(logIndex > lastLogIndex) {
            return new ArrayList<>();
        }
        int fromIndex = (int)logIndex;
        int endIndex = (int)lastLogIndex;
        List<Entry> list = new ArrayList<>(endIndex - fromIndex + 1);
        while(fromIndex <= endIndex) {
            list.add(getLogEntry(fromIndex));
            fromIndex++;
        }
        return list;
    }

    @Override
    public boolean appendEntries(long preTerm, long preLogIndex, List<Entry> logs) {
        //心跳日志
        if(logs == null/* || logs.size() == 0*/) {
            if(isEmpty()) return false;

            EntryMeta entryMata = getEntryMata(lastLogIndex);
            return entryMata.getTerm() == preTerm && entryMata.getLogIndex() == preLogIndex;
        }
        long lastLogIndexTemp = lastLogIndex;
        for (Entry entry : logs) {
            boolean result = appendEntry(entry, preTerm, preLogIndex);
            if(!result) {
                logger.warn("index sequence of log entries receiving from leader is not match, " +
                        "preTerm is {}, preLogIndex is {}, but current logIndex is {}", preTerm, preLogIndex, entry.getIndex());
                //回滚
                deleteLogEntriesFrom(lastLogIndexTemp + 1);
                System.out.println(lastLogIndex);
                for (Entry log : logs) {
                    System.out.print(log.getIndex() + " ");
                }
                System.out.println();
                return false;
            }
            preTerm = entry.getTerm();
            preLogIndex = entry.getIndex();
        }
        return true;
    }

//    @Override
//    public boolean appendEmptyEntry(Entry entry) {
//        return false;
//    }
//
//    @Override
//    public boolean deleteLogEntriesFrom(long logIndex) {
//        return false;
//    }

    @Override
    public boolean match(long logIndex, long preTerm, long preLogIndex) {
        EntryMeta preEntryMeta = getPreEntryMeta(logIndex);
        return preEntryMeta.getLogIndex() == preLogIndex && preEntryMeta.getTerm() == preTerm;
    }

    @Override
    public boolean isEmpty() {
        return lastLogIndex == 0;
    }
}
