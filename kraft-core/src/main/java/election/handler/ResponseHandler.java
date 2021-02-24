package election.handler;

import election.node.NodeId;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteResultMessage;

public interface ResponseHandler {
    void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage);
    void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage, NodeId fromId);
}
