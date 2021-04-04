package server.handler;

import common.message.GeneralResult;
import common.message.Response;
import common.message.ResponseType;
import common.message.StatusCode;
import common.message.command.DelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;

public class DelCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelCommandHandler.class);
    private KVStore kvStore;

    public DelCommandHandler(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public Response handle(Object command) {
        DelCommand delCommand = (DelCommand) command;
        kvStore.del(delCommand.getKey());
        logger.debug("key [{}] was deleted", delCommand.getKey());
        return new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK));
    }
}
