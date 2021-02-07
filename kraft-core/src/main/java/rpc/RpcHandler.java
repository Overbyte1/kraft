package rpc;

import election.NodeId;
import election.log.LogEntry;

import java.util.List;

public interface RpcHandler {
    void sendRequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm);
    void sendAppendEntriesMessage(long term, NodeId leaderId, long preLogIndex, long preLogTerm,
                                  List<LogEntry> logEntryList, long leaderCommit);
}
