package rpc;

import election.node.NodeId;
import election.log.LogEntry;

import java.util.List;

public interface RpcHandler {
    void sendRequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm);
    void sendAppendEntriesMessage(long term, NodeId leaderId, long preLogIndex, long preLogTerm,
                                  List<LogEntry> logEntryList, long leaderCommit);
    void sendRequestVoteResultMessage(long term, boolean voteGranted);
    void sendAppendEntriesResultMessage(long term, boolean success);
}
