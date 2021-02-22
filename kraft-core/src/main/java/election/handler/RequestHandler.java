package election.handler;

import rpc.message.AppendEntriesMessage;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

//TODO：取个好名字
public interface RequestHandler extends MessageHandler {

    AppendEntriesResultMessage handleAppendEntriesRequest(AppendEntriesMessage appendRequestMsg);

    RequestVoteResultMessage handleRequestVoteRequest(RequestVoteMessage requestVoteMessage);

}
