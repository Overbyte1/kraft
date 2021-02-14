package election.handler;

import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteResultMessage;

public interface ResponseHandler {
    void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage);
    void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage);
}
