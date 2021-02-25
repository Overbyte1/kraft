package election.log;

import election.log.entry.Entry;
import election.log.entry.EntryMeta;

import java.util.List;

public interface LogStore {
    Entry getLogEntry(long logIndex);

    Entry getLastEntry();

    long getLastLogIndex();

    EntryMeta getEntryMata(long logIndex);

    List<Entry> getLogEntriesFrom(long logIndex);

    boolean appendEntries(List<Entry> logs);

    void appendEntry(Entry entry);

    boolean deleteLogEntriesFrom(long logIndex);

    boolean match(long logIndex, long preTerm, long preLogIndex);

}
