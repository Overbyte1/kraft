package election.handler;

import org.slf4j.Logger;
import rpc.message.AppendEntriesResultMessage;
import rpc.message.RequestVoteMessage;
import rpc.message.RequestVoteResultMessage;

public class AbstractMessageHandler implements RequestHandler, ResponseHandler {
    private final Logger logger;
    public AbstractMessageHandler(Logger logger) {
        this.logger = logger;
    }
    @Override
    public void handle(Object message) {
        //根据消息类型选择不同的处理器
        Class<?> messageClass = message.getClass();
        if(messageClass == AppendEntriesResultMessage.class) {
            handleAppendEntriesRequest((AppendEntriesResultMessage) message);
        } else if(messageClass == RequestVoteMessage.class) {
            handleRequestVoteRequest((RequestVoteMessage)message);
            //TODO：添加其余判断
        } else {
            logger.warn("Can not handle this message, message = {}", message);
        }
    }

    @Override
    public void handleAppendEntriesRequest(AppendEntriesResultMessage appendRequestMsg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage) {
        throw new UnsupportedOperationException();
    }
}
