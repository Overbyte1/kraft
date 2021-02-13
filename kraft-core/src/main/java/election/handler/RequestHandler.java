package election.handler;

import rpc.message.AbstractRequest;
import rpc.message.AbstractResponse;

//TODO：取个好名字
public interface RequestHandler extends Handler {

    void handleAppendEntriesRequest(AbstractRequest request);

    void handleRequestVote(AbstractRequest request);

}
