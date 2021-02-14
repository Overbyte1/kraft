package election.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

//TODO:remove
public class LeaderMessageHandler extends AbstractMessageHandler implements RequestHandler, ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(LeaderMessageHandler.class);

    public LeaderMessageHandler(Logger logger) {
        super(logger);
    }

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
