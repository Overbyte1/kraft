package election.log.store;

import election.log.entry.Entry;

/**
 * 基于文件进行存储，需要考虑：
 * 1. 文件的存储格式、位置、命名
 * 2. 写回文件，读取文件的时机
 */
public class FileLogStore extends AbstractLogStore implements LogStore {
    @Override
    public Entry getLogEntry(long logIndex) {
        return null;
    }

    @Override
    public boolean appendEmptyEntry(Entry entry) {
        return false;
    }

    @Override
    public boolean appendEntry(Entry entry, long preTerm, long preLogIndex) {
        return false;
    }

    @Override
    public boolean deleteLogEntriesFrom(long logIndex) {
        return false;
    }
}
