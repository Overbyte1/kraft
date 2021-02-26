package election.handler;

import rpc.message.AbstractMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteResultMessage;

public interface ResponseHandler {
    void handleRequestVoteResult(AbstractMessage<RequestVoteResultMessage> message);
    void handleAppendEntriesResult(AbstractMessage<AppendEntriesResultMessage> message);
}
