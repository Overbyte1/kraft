package server.handler;

import common.message.response.MultiPayloadResult;
import common.message.response.Response;
import common.message.command.MGetCommand;
import common.message.response.ResponseType;
import common.message.response.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;

import java.util.Arrays;

public class MGetCommandHandler extends AbstractTransactionCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(MGetCommandHandler.class);
    //private KVStore kvStore;

    public MGetCommandHandler(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }


    @Override
    public Response doHandle(Object command, KVStore kvStore) {
        MGetCommand mGetCommand = (MGetCommand) command;
        String[] keys = mGetCommand.getKeys();
        byte[][] payload = new byte[keys.length][];
        logger.debug("do mget operation, keys: {}", Arrays.toString(keys));
        for(int i = 0; i < payload.length; i++) {
            payload[i] = kvStore.get(keys[i]);
            logger.debug("do get: [{}]-[{}]", keys[i], payload[i]);
        }
        return new Response(ResponseType.SUCCEED, new MultiPayloadResult(StatusCode.SUCCEED_OK, payload));
    }
}
