package election.handler;

import election.node.NodeGroup;
import election.node.NodeId;
import org.slf4j.Logger;
import rpc.NodeEndpoint;
import rpc.RpcHandler;
import rpc.message.*;

public class AbstractMessageHandler implements RequestHandler, ResponseHandler {
    private final Logger logger;
    private final NodeGroup nodeGroup;
    private final RpcHandler rpcHandler;
    public AbstractMessageHandler(Logger logger, NodeGroup nodeGroup, RpcHandler rpcHandler) {
        this.logger = logger;
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
    }
    @Override
    public void handle(Object message) {
        AbstractMessage abstractMessage = (AbstractMessage) message;
        NodeId nodeId = abstractMessage.getNodeId();
        NodeEndpoint nodeEndpoint = nodeGroup.getGroupMember(nodeId).getNodeEndpoint();
        //根据消息类型选择不同的处理器
        Class<?> messageClass = abstractMessage.getBody().getClass();
        if(messageClass == RequestVoteMessage.class) {
            RequestVoteResultMessage resultMessage = handleRequestVoteRequest((RequestVoteMessage) abstractMessage.getBody());
            rpcHandler.sendRequestVoteResultMessage(resultMessage, nodeEndpoint);
        } else if(messageClass == RequestVoteResultMessage.class) {
            handleRequestVoteResult((RequestVoteResultMessage) abstractMessage.getBody());
        } else if(messageClass == AppendEntriesMessage.class) {
            handleAppendEntriesRequest((AppendEntriesMessage)abstractMessage.getBody());
        }else if(messageClass == AppendEntriesResultMessage.class) {
            handleAppendEntriesResult((AppendEntriesResultMessage) abstractMessage.getBody(), nodeId);
        }
        //TODO：添加其余判断

        else {
            logger.warn("Can not handle this message, message = {}", abstractMessage.getBody());
        }
    }

    @Override
    public AppendEntriesResultMessage handleAppendEntriesRequest(AppendEntriesMessage appendRequestMsg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestVoteResultMessage handleRequestVoteRequest(RequestVoteMessage requestVoteMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleRequestVoteResult(RequestVoteResultMessage voteResultMessage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleAppendEntriesResult(AppendEntriesResultMessage appendEntriesResultMessage, NodeId fromId) {
        throw new UnsupportedOperationException();
    }
}
