package election.handler;

import rpc.message.*;

//TODO：取个好名字
public interface RequestHandler extends MessageHandler {

    AppendEntriesResultMessage handleAppendEntriesRequest(AbstractMessage<AppendEntriesMessage> message);

    RequestVoteResultMessage handleRequestVoteRequest(AbstractMessage<RequestVoteMessage> message);

}
