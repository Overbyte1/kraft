package rpc;

import election.NodeId;
import election.log.LogEntry;

import java.util.List;

public class RpcHandlerImpl implements RpcHandler {
    @Override
    public void sendRequestVoteMessage(long term, NodeId candidateId, long lastLogIndex, long lastLogTerm) {

    }

    @Override
    public void sendAppendEntriesMessage(long term, NodeId leaderId, long preLogIndex, long preLogTerm,
                                         List<LogEntry> logEntryList, long leaderCommit) {

    }
}
