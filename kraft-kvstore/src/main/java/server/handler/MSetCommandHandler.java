package server.handler;

import common.message.command.MSetCommand;
import common.message.response.*;
import election.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;
import utils.SerializationUtil;

import java.io.IOException;
import java.util.Arrays;

public class MSetCommandHandler extends AbstractTransactionCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(MSetCommandHandler.class);
    //private KVStore kvStore;
    private Node node;

    public MSetCommandHandler(KVStore kvStore, Node node) {
        //super(kvStore);
        this.kvStore = kvStore;
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        MSetCommand mSetCommand = (MSetCommand) command;
        logger.debug("mset operation: [{}]-[{}]", Arrays.toString(mSetCommand.getKeys()), Arrays.toString(mSetCommand.getValues()));
        try {
            node.appendLog(SerializationUtil.encodes(command));
        } catch (IOException e) {
            logger.warn("fail to serialize SetCommand object: {}, cause is: {}", mSetCommand, e.getMessage());
            return new Response(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR);
        }
        return null;
    }


    @Override
    public Response doHandle(Object command, KVStore kvStore) {
        MSetCommand mSetCommand = (MSetCommand) command;
        String[] keys = mSetCommand.getKeys();
        byte[][] values = mSetCommand.getValues();
        logger.debug("start do mset operation, the number of keys is: {}", keys.length);
        for(int i = 0; i < keys.length; i++) {
            kvStore.set(keys[i], values[i]);
            logger.debug("do set: [{}]-[{}]", keys[i], values[i]);
        }
        return new Response(ResponseType.SUCCEED, new NoPayloadResult(StatusCode.SUCCEED_OK));
    }
}
