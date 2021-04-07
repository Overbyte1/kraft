package server.handler;

import common.message.command.SetCommand;
import common.message.response.*;
import election.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;
import utils.SerializationUtil;

import java.io.IOException;

public class SetCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetCommandHandler.class);

    private KVStore kvStore;
    private Node node;

    public SetCommandHandler(KVStore kvStore, Node node) {
        this.kvStore = kvStore;
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        SetCommand setCommand = (SetCommand) command;
        logger.debug("set operation: [{}]-[{}]", setCommand.getKey(), new String(setCommand.getValue()));
        try {
            node.appendLog(SerializationUtil.encodes(setCommand));
        } catch (IOException e) {
                logger.warn("fail to serialize SetCommand object: {}, cause is: {}", setCommand, e.getMessage());
                return new Response(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR);
        }
        return null;
    }

    @Override
    public Response doHandle(Object command) {
        SetCommand setCommand = (SetCommand)command;
        kvStore.set(setCommand.getKey(), setCommand.getValue());
        logger.debug("do set operation: [{}]-[{}]", setCommand.getKey(), new String(setCommand.getValue()));
        return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
    }
}
