package election.log;

import election.log.entry.LogEntry;

import java.util.List;

public interface LogStore {
    LogEntry getLogEntry(long logIndex);

    List<LogEntry> getLogEntriesFrom(long logIndex);

    boolean appendEntries(List<LogEntry> logs);

    boolean deleteLogEntriesFrom(long logIndex);

    boolean match(long logIndex, long preTerm, long preLogIndex);
}
