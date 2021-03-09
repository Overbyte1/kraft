package election.log.store;

import election.LogIndexOutOfBoundsException;
import election.log.entry.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//TODO:保证线程安全
public class MemoryLogStore extends AbstractLogStore implements LogStore {
    private static final Logger logger = LoggerFactory.getLogger(MemoryLogStore.class);
    private List<Entry> entryList;
    //第一条日志的index为1
    private int lastLogIndex;
    private int offset;

    public MemoryLogStore() {
        entryList = new ArrayList<>();
        lastLogIndex = 0;
        offset = 0;
    }

    @Override
    public Entry getLogEntry(long logIndex) {
        if(lastLogIndex < 1 || logIndex > lastLogIndex || logIndex < 1) {
            throw new LogIndexOutOfBoundsException("" + logIndex);
        }
        return entryList.get(toListIndex(logIndex));
    }

//    @Override
//    public  Entry getLastEntry() {
//        if(lastLogIndex < 1) {
//            throw new LogIndexOutOfBoundsException("last log index is" + lastLogIndex + ", no any log");
//        }
//        int listIndex = toListIndex(lastLogIndex);
//
//        return entryList.get(listIndex);
//    }

//    @Override
//    public  EntryMeta getEntryMata(long logIndex) {
//        if(lastLogIndex < 1 || logIndex > lastLogIndex || logIndex < 1) {
//            throw new LogIndexOutOfBoundsException("" + logIndex);
//        }
//        Entry entry = getLogEntry(logIndex);
//        return new EntryMeta(entry.getIndex(), entry.getTerm());
//    }

//    @Override
//    public EntryMeta getPreEntryMeta(long logIndex) {
//        //索引为0的 term=0, logIndex=0，但只能在进行匹配时使用
//        if(logIndex == 1) {
//            return new EntryMeta(0, 0);
//        }
//        return getEntryMata(logIndex - 1);
//    }

//    @Override
//    public  List<Entry> getLogEntriesFrom(long logIndex) {
//        if(logIndex > lastLogIndex) {
//            return new ArrayList<>();
//        }
//        int fromIndex = toListIndex(logIndex);
//        int endIndex = entryList.size();
//        List<Entry> list = new ArrayList<>(endIndex - fromIndex + 1);
//        while(fromIndex < endIndex) {
//            list.add(entryList.get(fromIndex));
//            fromIndex++;
//        }
//        return list;
//    }

//    /**
//     * 附加日志，需要保证线程安全
//     * @param preTerm
//     * @param preLogIndex
//     * @param logs
//     * @return
//     */
//    @Override
//    public  boolean appendEntries(long preTerm, long preLogIndex, List<Entry> logs) {
//        try {
//            //心跳日志
//            if(logs == null || logs.size() == 0) {
//                if(isEmpty()) return false;
//
//                EntryMeta entryMata = getEntryMata(lastLogIndex);
//                return entryMata.getTerm() == preTerm && entryMata.getLogIndex() == preLogIndex;
//            }
//            //判断index位置处的preTerm与preLogIndex是否匹配
//            long index = logs.get(0).getIndex();
//            if(match(index, preTerm, preLogIndex)) {
//                deleteLogEntriesFrom(index);
//                for (Entry entry : logs) {
//                    //index应单调递增，间隔1
//                    if(entry.getIndex() == preLogIndex + 1) {
//                        entryList.add(entry);
//                        preLogIndex = entry.getIndex();
//                    } else {
//                        logger.warn("log index is not match, logs: {}", logs);
//                        for(int i = entryList.size() - 1; i > lastLogIndex; i--) {
//                            entryList.remove(i);
//                        }
//                        return false;
//                    }
//                }
//                //boolean res = entryList.addAll(logs);
//                lastLogIndex += logs.size();
//                logger.debug("succeed appending {} entries to log, current log: {}", logs.size(), entryList);
//                return true;
//            }
//        } catch (Exception exception) {
//            logger.debug("fail to append entries {}, cause is: ", logs, exception.getMessage());
//        }
//        return false;
//    }

    @Override
    public boolean appendEntry(Entry entry, long preTerm, long preLogIndex) {
        return false;
    }

    @Override
    public  boolean appendEmptyEntry(Entry entry) {
        lastLogIndex++;
        entry.setIndex(lastLogIndex);
        logger.debug("entry {} was append", entry);
        entryList.add(entry);
        return true;
    }

    @Override
    public  boolean deleteLogEntriesFrom(long logIndex) {
        int fromIndex = toListIndex(logIndex);
        for(int i = entryList.size() - 1; i >= fromIndex; i--) {
            entryList.remove(i);
            lastLogIndex--;
        }
        return true;
    }

//    @Override
//    public  boolean match(long logIndex, long preTerm, long preLogIndex) {
////        if(logIndex == 1 && preTerm == 0 && lastLogIndex == 0 && preLogIndex == 0) {
////            return true;
////        }
//        if(preLogIndex < 0) {
//            return false;
//        }
//        try {
//            EntryMeta entryMata = getPreEntryMeta(logIndex);
//            return entryMata.getLogIndex() == preLogIndex && entryMata.getTerm() == preTerm;
//        } catch (Exception e) {
//            return false;
//        }
//    }

//    @Override
//    public  long getLastLogIndex() {
//        return lastLogIndex;
//    }
//    @Override
//    public  boolean isEmpty() {
//        return lastLogIndex == 0;
//    }
    public  int size() {
        return entryList.size();
    }

    private  int toListIndex(long logIndex) {
        return (int) (logIndex - offset - 1);
    }

    private  long toLogIndex(int listIndex) {
        return listIndex + offset + 1;
    }


}
