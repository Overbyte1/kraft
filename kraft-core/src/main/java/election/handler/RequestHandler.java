package election.handler;

import rpc.message.*;


public interface RequestHandler extends MessageHandler {

    AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message);

    RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message);

}
