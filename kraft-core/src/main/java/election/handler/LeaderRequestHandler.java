package election.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.message.AbstractRequest;

/**
 * 负责Leader接收消息后的处理，其需要处理
 */
public class LeaderRequestHandler implements RequestHandler {
    private final Logger logger = LoggerFactory.getLogger(LeaderRequestHandler.class);

    @Override
    public void handle(AbstractRequest request) {
        //根据消息类型选择不同的处理器
        int type = request.getType();
        Object msg = request.getRequestBody();
    }

    @Override
    public void handleAppendEntriesRequest(AbstractRequest request) {

    }

    @Override
    public void handleRequestVote(AbstractRequest request) {

    }
}
