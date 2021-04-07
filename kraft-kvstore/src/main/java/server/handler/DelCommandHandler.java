package server.handler;

import common.message.command.DelCommand;
import common.message.response.*;
import election.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;
import utils.SerializationUtil;

import java.io.IOException;

public class DelCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelCommandHandler.class);
    private KVStore kvStore;
    private Node node;

    public DelCommandHandler(KVStore kvStore, Node node) {
        this.kvStore = kvStore;
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        DelCommand delCommand = (DelCommand) command;
        logger.debug("del operation: [{}]", delCommand.getKey());
        if(!kvStore.containsKey(delCommand.getKey())) {
            return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_NO_CONTENT));
        }
        try {
            node.appendLog(SerializationUtil.encodes(delCommand));
        } catch (IOException e) {
            logger.warn("fail to serialize SetCommand object: {}, cause is: {}", delCommand, e.getMessage());
            return new Response(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR);
        }
        return null;
    }

    @Override
    public Response doHandle(Object command) {
        DelCommand delCommand = (DelCommand) command;
        kvStore.del(delCommand.getKey());
        logger.debug("do del: [{}]", delCommand.getKey());
        return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
    }
}
