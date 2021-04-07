package server.handler;

import common.message.command.MDelCommand;
import common.message.response.*;
import election.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;
import utils.SerializationUtil;

import java.io.IOException;
import java.util.Arrays;

public class MDelCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(MDelCommandHandler.class);
    private KVStore kvStore;
    private Node node;

    public MDelCommandHandler(KVStore kvStore, Node node) {
        this.kvStore = kvStore;
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        MDelCommand mDelCommand = (MDelCommand)command;
        boolean contains = false;
        for(String key : mDelCommand.getKey()) {
            if(kvStore.containsKey(key)) {
                contains = true;
                break;
            }
        }
        if(!contains) {
            return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
        }
        logger.debug("mdel operation: {}", Arrays.toString(mDelCommand.getKey()));
        try {
            node.appendLog(SerializationUtil.encodes(mDelCommand));
        } catch (IOException e) {
            logger.warn("fail to serialize SetCommand object: {}, cause is: {}", mDelCommand, e.getMessage());
            return new Response(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR);
        }
        return null;
    }

    @Override
    public Response doHandle(Object command) {
        MDelCommand mDelCommand = (MDelCommand) command;
        for(String key : mDelCommand.getKey()) {
            logger.debug("do del: [{}]", key);
            kvStore.del(key);
        }
        return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
    }
}
