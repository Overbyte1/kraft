package server.handler;

import common.message.GeneralResult;
import common.message.Response;
import common.message.ResponseType;
import common.message.StatusCode;
import common.message.command.SetCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.KVStore;

public class SetCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetCommandHandler.class);

    private KVStore kvStore;

    public SetCommandHandler(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public Response handle(Object command) {
        SetCommand setCommand = (SetCommand)command;
        kvStore.set(setCommand.getKey(), setCommand.getValue());
        logger.debug("key/value [{}/{}] was set", setCommand.getKey(), new String(setCommand.getValue()));
        return new Response(ResponseType.SUCCEED, new GeneralResult(StatusCode.SUCCEED_OK));
    }
}
