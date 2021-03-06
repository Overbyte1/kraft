package election.handler;

import election.node.NodeGroup;
import election.node.NodeId;
import org.slf4j.Logger;
import rpc.NodeEndpoint;
import rpc.RpcHandler;
import rpc.message.*;
import schedule.SingleThreadTaskExecutor;
import schedule.TaskExecutor;

public abstract class AbstractMessageHandler implements RequestHandler, ResponseHandler {
    private final Logger logger;
    private final NodeGroup nodeGroup;
    private final RpcHandler rpcHandler;
    private TaskExecutor taskExecutor;
    public AbstractMessageHandler(Logger logger, NodeGroup nodeGroup, RpcHandler rpcHandler, TaskExecutor taskExecutor) {
        this.logger = logger;
        this.nodeGroup = nodeGroup;
        this.rpcHandler = rpcHandler;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void handle(Object message) {
        AbstractMessage abstractMessage = (AbstractMessage) message;
        NodeId nodeId = abstractMessage.getNodeId();
        NodeEndpoint nodeEndpoint = nodeGroup.getGroupMember(nodeId).getNodeEndpoint();
        //根据消息类型选择不同的处理器
        //TODO:策略模式？
        Class<?> messageClass = abstractMessage.getBody().getClass();
        if(messageClass == RequestVoteMessage.class) {
            taskExecutor.submit(
                    () -> {
                        RequestVoteResultMessage resultMessage = handleRequestVoteRequest(abstractMessage);
                        rpcHandler.sendRequestVoteResultMessage(resultMessage, nodeEndpoint);

                    }
            );
        } else if(messageClass == RequestVoteResultMessage.class) {
            taskExecutor.submit(
                    () -> {
                        handleRequestVoteResult(abstractMessage);
                    }
            );

        } else if(messageClass == AppendEntriesMessage.class) {

            taskExecutor.submit(
                    () -> {
                        AppendEntriesResultMessage entriesResultMessage = handleAppendEntriesRequest(abstractMessage);
                        rpcHandler.sendAppendEntriesResultMessage(entriesResultMessage, nodeEndpoint);

                    }
            );
        }else if(messageClass == AppendEntriesResultMessage.class) {

            taskExecutor.submit(
                    () -> {
                        handleAppendEntriesResult(abstractMessage);
                    }
            );
        }
        //TODO：添加其余判断

        else {
            logger.warn("Can not handle this message, message = {}", abstractMessage.getBody());
        }
    }

}
