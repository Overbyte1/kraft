package election.log.store;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;

import java.util.List;

public interface LogStore {
    Entry getLogEntry(long logIndex);

    Entry getLastEntry();

    long getLastLogIndex();

    EntryMeta getEntryMata(long logIndex);

    EntryMeta getPreEntryMeta(long logIndex);

    List<Entry> getLogEntriesFrom(long logIndex);

    boolean appendEntries(long preTerm, long preLogIndex, List<Entry> logs);

    /**
     * 附加空日志
     * @param entry
     * @return
     */
    boolean appendEmptyEntry(Entry entry);


    boolean appendEntry(Entry entry, long preTerm, long preLogIndex);

    boolean deleteLogEntriesFrom(long logIndex);

    boolean match(long logIndex, long preTerm, long preLogIndex);

    boolean isEmpty();

}
