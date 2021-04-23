package log;

import com.google.common.util.concurrent.FutureCallback;
import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.EntryMeta;
import log.entry.GeneralEntry;
import log.store.LogStore;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;
import schedule.SingleThreadTaskExecutor;
import schedule.TaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LogImpl implements Log {
    private static final Logger logger = LoggerFactory.getLogger(LogImpl.class);
    private LogStore logStore;
    private StateMachine stateMachine;
    private volatile long commitIndex = 0;
    private long appliedIndex;
    private final NodeGroup nodeGroup;

    private static final long APPLY_ID_CHECK_INTERVAL = 2000;
    private final long applyIdCheckInterval;

    private final ReentrantLock mainLock = new ReentrantLock();
    private final Condition applyCondition = mainLock.newCondition();
    private final Thread applyThread;
    private volatile boolean running;

    private FutureCallback<Boolean> futureCallback = new FutureCallback<Boolean>() {
        @Override
        public void onSuccess(@Nullable Boolean result) {
            if(result) {
                appliedIndex++;
            }
        }

        @Override
        public void onFailure(Throwable t) {
            logger.info("fail to apply command, cause is: {}", t.getMessage());
        }
    };
    private final TaskExecutor taskExecutor = new SingleThreadTaskExecutor();

    public LogImpl(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup) {
        this(logStore, stateMachine, nodeGroup, APPLY_ID_CHECK_INTERVAL);
    }

    public LogImpl(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup, long applyIdCheckInterval) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.nodeGroup = nodeGroup;
        this.applyIdCheckInterval = applyIdCheckInterval;
        applyThread = new Thread(this::applyLoop ,"log application thread");
        running = true;
        applyThread.start();
    }

    /**
     * 增加commitIndex，只会被Leader调用,
     * commitIndex推进需要过半matchIndex以及term，只有日志条目的term和自己的term一致才能更新commitIndex
     *
     * 如果存在一个满足 N > commitIndex的 N，并且大多数的 matchIndex[i] ≥ N成立，
     * 并且 log[N].term == currentTerm 成立，那么令 commitIndex 等于这个 N
     *
     * @param currentTerm
     * @return
     */
    @Override
    public boolean advanceCommitForLeader(long currentTerm) {
        if(commitIndex == logStore.getLastLogIndex()) {
            return false;
        }
        long newCommitIndex = nodeGroup.getMajorMatchIndex();
        EntryMeta entryMata = logStore.getEntryMata(newCommitIndex);
        if(entryMata != null &&  currentTerm == entryMata.getTerm()) {
            logger.debug("advance commit index to {} from {}", newCommitIndex, commitIndex);
            //commitIndex = newCommitIndex;
            setCommitIndex(newCommitIndex);
            return true;
        }
        logger.debug("current commit index is: {}", commitIndex);
        return false;
    }
    private void setCommitIndex(long newCommitIndex) {
        if(newCommitIndex <= commitIndex) {
            return;
        }
        commitIndex = newCommitIndex;
        try {
            mainLock.lock();
            applyCondition.signal();
        } finally {
            mainLock.unlock();
        }
    }

    @Override
    public boolean updateReplicationState(ReplicationState replicationState, long addNum) {
        //TODO:需要确定replicationState matchIndex与nextIndex增加的幅度
        replicationState.incNextIndex(addNum);
        replicationState.setMatchIndex(replicationState.getNextIndex() - 1);

        return false;
    }

    @Override
    public long getLastLogIndex() {
        return logStore.getLastLogIndex();
    }

    /**
     * Raft 通过比较两份日志中最后一条日志条目的索引值和任期号定义谁的日志比较新：
     * 1. 如果两份日志最后的条目的任期号不同，那么任期号大的日志更加新。
     * 2. 如果两份日志最后的条目任期号相同，那么日志索引大的那个就更加新。
     * 3. 如果日志最大索引也相同，就认为远端节点的日志更加新。
     * @param lastTerm
     * @param lastLogIndex
     * @return
     */
    @Override
    public boolean isNewerThan(long lastTerm, long lastLogIndex) {
        if(logStore.isEmpty()) {
            return false;
        }
        EntryMeta entryMata = logStore.getEntryMata(logStore.getLastLogIndex());
        if(entryMata.getTerm() != lastTerm) {
            return lastTerm > entryMata.getTerm();
        }
        return lastLogIndex > entryMata.getLogIndex();
    }

    @Override
    public AppendEntriesMessage createAppendEntriesMessage(NodeId leaderId, long term, long nextIndex) {
        //
        AppendEntriesMessage message = null;
        //nextIndex初始值为1，在日志已经全部复制到Follower的情况下比lastLogIndex大1

        if(logStore.isEmpty()) {
            logger.warn("log store is empty, it should have at least one entry");
            message = new AppendEntriesMessage(term, leaderId, 0, 0, commitIndex, new ArrayList<>());
        } else {
            logger.debug("get log entries from index [{}]", nextIndex);
            EntryMeta preEntryMeta = logStore.getPreEntryMeta(nextIndex);
            List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
            message = new AppendEntriesMessage(term, leaderId, preEntryMeta.getTerm(),
                    preEntryMeta.getLogIndex(), commitIndex, entryList);
        }
        return message;
    }

    @Override
    public AppendEntriesMessage createEmptyAppendEntriesMessage(NodeId leaderId, long term, long nextIndex) {
        EntryMeta preEntryMeta = logStore.getPreEntryMeta(nextIndex);
        return new AppendEntriesMessage(term, leaderId, preEntryMeta.getTerm(),
                preEntryMeta.getLogIndex(), commitIndex, new ArrayList<>());

    }

    @Override
    public RequestVoteMessage createRequestVoteMessage(NodeId candidateId, long term) {
        long lastLogIndex = logStore.getLastLogIndex();
        long lastLogTerm = 0;
        if(lastLogIndex > 0) {
            EntryMeta entryMata = logStore.getEntryMata(lastLogIndex);
            lastLogTerm = entryMata.getTerm();
        }

        return new RequestVoteMessage(term, candidateId, lastLogIndex,  lastLogTerm);
    }

    @Override
    public EmptyEntry appendEmptyEntry(long term) {
        EmptyEntry entry = new EmptyEntry(term, logStore.getLastLogIndex() + 1);
        logStore.appendEmptyEntry(entry);
        return entry;
    }

    /**
     * Leader附加一条常规日志，只会被Leader调用
     * @param term
     * @param command
     * @return
     */
    @Override
    public boolean appendGeneralEntry(long term, byte[] command) {
        GeneralEntry entry = new GeneralEntry(term, logStore.getLastLogIndex() + 1, command);
        logStore.appendEmptyEntry(entry);
        return true;
    }

    @Override
    public boolean appendGeneralEntriesFromLeader(long preTerm, long preLogIndex, List<Entry> entryList,
                                                  long leaderCommit) {
        boolean result = logStore.appendEntries(preTerm, preLogIndex, entryList);
        if(result) {
            //更新commitIndex、appliedIndex
            long oldCommitIndex = commitIndex;
            long newCommitIndex = Math.min(leaderCommit, logStore.getLastLogIndex());
            setCommitIndex(newCommitIndex);
            if(oldCommitIndex != newCommitIndex) {
                logger.debug("advance commit index to {} from {}", newCommitIndex, oldCommitIndex);
            }
//            if(appliedIndex < commitIndex) {
//
//                for (Entry entry : entryList) {
//                    if(entry instanceof GeneralEntry)
//                        apply(((GeneralEntry)entry).getCommandBytes());
//                }
//            }
        }
        if(!result) {
            logger.debug("fail to append entry {} from leader", entryList);
        } else {
            logger.debug("succeed to append entry {} from leader", entryList);
        }
        return result;
    }


    @Override
    public boolean apply(byte[] command) {
        logger.debug("appendLog command");
        try {
            boolean result = stateMachine.apply(command);
            if(!result)  {
                logger.info("fail to apply log, command: {}", new String(command));
            }
            return result;
        } catch (Exception exception) {
            logger.info("exception was caught when applying log, cause is: {}", exception.getMessage());
        }
        return false;
    }

    private void applyLoop() {
        logger.debug("log application thread is running");
        while (running) {
            logger.info("applied index is [{}]", appliedIndex);
            if(appliedIndex == commitIndex) {
                try {
                    mainLock.lock();
                    applyCondition.await(applyIdCheckInterval, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.info("application log thread was interrupted");
                } finally {
                    mainLock.unlock();
                }
            } else {
                Entry entry = logStore.getLogEntry(appliedIndex + 1);
                if(entry instanceof GeneralEntry) {
                    if(apply(((GeneralEntry)entry).getCommandBytes())) {
                        appliedIndex++;
                    }
                    //如果应用到状态机失败则需要重试
                } else {
                    appliedIndex++;
                }

            }
        }
        logger.debug("log application thread was terminated");
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @Override
    public void close() {
        running = false;
    }
}
