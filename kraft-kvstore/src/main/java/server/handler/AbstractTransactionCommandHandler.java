package server.handler;

import common.message.response.Response;
import server.store.KVStore;

public abstract class AbstractTransactionCommandHandler implements TransactionCommandHandler {
    protected KVStore kvStore;

    @Override
    public Response doHandle(Object command) {
        return doHandle(command, kvStore);
    }
}
