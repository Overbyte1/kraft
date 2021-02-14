package election.handler;

import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;

//TODO：取个好名字
public interface RequestHandler extends MessageHandler {

    void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg);

    void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage);

}
