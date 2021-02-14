package election.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.*;
//TODO:remove
public class FollowerMessageHandler implements RequestHandler, ResponseHandler {
    private final Logger logger = LoggerFactory.getLogger(FollowerMessageHandler.class);


    @Override
    public void handle(Object message) {

    }

    @Override
    public void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg) {

    }

    @Override
    public void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {

    }

    @Override
    public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {

    }

    @Override
    public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage) {

    }
}
