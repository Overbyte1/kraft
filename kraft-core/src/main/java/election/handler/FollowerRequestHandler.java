package election.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.*;

public class FollowerRequestHandler implements RequestHandler {
    private final Logger logger = LoggerFactory.getLogger(FollowerRequestHandler.class);


    @Override
    public void handle(AbstractRequest request) {

    }

    @Override
    public void handleAppendEntriesRequest(AbstractRequest request) {

    }

    @Override
    public void handleRequestVote(AbstractRequest request) {

    }
}
