package log;

import log.entry.EmptyEntry;
import log.entry.Entry;
import log.entry.EntryMeta;
import log.entry.GeneralEntry;
import log.store.LogStore;
import election.node.NodeGroup;
import election.node.NodeId;
import election.node.ReplicationState;
import election.statemachine.StateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AppendEntriesMessage;
import rpc.message.RequestVoteMessage;

import java.util.ArrayList;
import java.util.List;

public class LogImpl implements Log {
    private static final Logger logger = LoggerFactory.getLogger(LogImpl.class);
    private LogStore logStore;
    private StateMachine stateMachine;
    private long commitIndex = 0;
    private long appliedIndex;
    private NodeGroup nodeGroup;

    //TODO:remove commitIndex
    public LogImpl(LogStore logStore, StateMachine stateMachine, long commitIndex, NodeGroup nodeGroup) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.commitIndex = commitIndex;
        this.nodeGroup = nodeGroup;
    }
    public LogImpl(LogStore logStore, StateMachine stateMachine, NodeGroup nodeGroup) {
        this.logStore = logStore;
        this.stateMachine = stateMachine;
        this.nodeGroup = nodeGroup;
    }

    /**
     * 增加commitIndex，只会被Leader调用,
     * TODO：commitIndex推进需要过半matchIndex以及term，只有日志条目的term和自己的term一致才能更新commitIndex
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
            commitIndex = newCommitIndex;
            return true;
        }
        logger.debug("current commit index is: {}", commitIndex);
        return false;
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
            EntryMeta preEntryMeta = logStore.getPreEntryMeta(nextIndex);
            List<Entry> entryList = logStore.getLogEntriesFrom(nextIndex);
            message = new AppendEntriesMessage(term, leaderId, preEntryMeta.getTerm(),
                    preEntryMeta.getLogIndex(), commitIndex, entryList);
        }
        return message;
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
            commitIndex = Math.min(leaderCommit, logStore.getLastLogIndex());
            if(oldCommitIndex != commitIndex) {
                logger.debug("advance commit index to {} from {}", commitIndex, oldCommitIndex);
            }
            if(appliedIndex < commitIndex) {
                //TODO：异步执行命令
                for (Entry entry : entryList) {
                    if(entry instanceof GeneralEntry)
                        apply(((GeneralEntry)entry).getCommandBytes());
                }
            }
        }
        if(!result) {
            logger.debug("fail to append entry {} from leader", entryList);
        } else {
            logger.debug("succeed to append entry {} from leader", entryList);
        }
        return result;
    }


    @Override
    public void apply(byte[] command) {
        logger.debug("appendLog command");
    }

    @Override
    public void registerStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }
}
