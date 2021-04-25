package server.handler;

import common.message.command.TrxCommand;
import common.message.response.FailureResult;
import common.message.response.Response;
import common.message.response.ResponseType;
import election.node.Node;
import org.rocksdb.RocksDBException;
import org.rocksdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.store.TransactionKVStore;
import utils.SerializationUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class TrxCommandHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(TrxCommandHandler.class);
    private Node node;
    private TransactionKVStore kvStore;
    private Map<Class<?>, CommandHandler> commandHandlerMap;

    public TrxCommandHandler(Node node, TransactionKVStore kvStore, Map<Class<?>, CommandHandler> commandHandlerMap) {
        this.node = node;
        this.kvStore = kvStore;
        this.commandHandlerMap = commandHandlerMap;
    }

    @Override
    public Response handleCommand(Object command) {
        try {
            node.appendLog(SerializationUtil.encodes(command));
        } catch (IOException e) {
            logger.warn("fail to serialize TrxCommand object: {}, cause is: {}", command, e.getMessage());
            return new Response(ResponseType.FAILURE, FailureResult.SERVER_INTERVAL_ERROR);
        }
        return null;
    }

    @Override
    public Response doHandle(Object command) {
        TrxCommand trxCommand = (TrxCommand)command;
        Transaction transaction = null;
        try {
            transaction = kvStore.begin();
            List<Object> commands = trxCommand.getCommands();
            Response<?>[] responses = new Response[commands.size()];
            int index = 0;
            for (Object cmd : commands) {
                Response<?> resp = commandHandlerMap.get(cmd.getClass()).handleCommand(cmd);
                responses[index] = resp;
                index++;
            }
            kvStore.commit(transaction);
            return new Response(ResponseType.SUCCEED, responses);
        } catch (Exception e) {
            try {
                if(transaction != null)
                    kvStore.rollback(transaction);
            } catch (RocksDBException rocksDBException) {
                rocksDBException.printStackTrace();
            }
        }
        return new Response(ResponseType.FAILURE, FailureResult.TRX_FAIL);
    }
}
