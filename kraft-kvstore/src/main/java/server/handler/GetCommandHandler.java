package server.handler;

import common.message.response.SinglePayloadResult;
import common.message.response.Response;
import common.message.response.ResponseType;
import common.message.response.StatusCode;
import common.message.command.GetCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;

public class GetCommandHandler extends AbstractTransactionCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GetCommandHandler.class);
    //private KVStore kvStore;

    public GetCommandHandler(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public Response handleCommand(Object command) {
        logger.debug("get operation: [{}]", ((GetCommand)command).getKey());
        return doHandle(command);
    }


    @Override
    public Response doHandle(Object command, KVStore kvStore) {
        GetCommand getCommand = (GetCommand) command;
        byte[] bytes = kvStore.get(getCommand.getKey());
        logger.debug("do get operation: [{}]-[{}]", ((GetCommand) command).getKey(), bytes);
        return new Response(ResponseType.SUCCEED, new SinglePayloadResult(StatusCode.SUCCEED_OK, bytes));
    }
}
