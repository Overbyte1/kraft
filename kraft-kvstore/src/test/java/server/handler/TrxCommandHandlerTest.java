package server.handler;

import common.message.command.*;
import election.node.Node;
import org.junit.Test;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDB;
import server.NodeMock;
import server.store.RocksDBTransactionKVStore;
import server.store.TransactionKVStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TrxCommandHandlerTest {
    @Test
    public void doHandle() throws RocksDBException {
        Node node = null;
        Options options = new Options();
        TransactionKVStore kvStore = new RocksDBTransactionKVStore(options);
        Map<Class<?>, CommandHandler> handlerMap = new HashMap<>();
        handlerMap.put(GetCommand.class, new GetCommandHandler(kvStore));
        handlerMap.put(SetCommand.class, new SetCommandHandler(kvStore, node));
        handlerMap.put(DelCommand.class, new DelCommandHandler(kvStore, node));
        handlerMap.put(MDelCommand.class, new MDelCommandHandler(kvStore, node));
        handlerMap.put(MSetCommand.class, new MSetCommandHandler(kvStore, node));
        handlerMap.put(MGetCommand.class, new MGetCommandHandler(kvStore));
        handlerMap.put(LeaderCommand.class, new LeaderCommandHandler(node));
        handlerMap.put(ServerListCommand.class, new ServerListCommandHandler(node));
        handlerMap.put(PingCommand.class, new PingCommandHandler());
        handlerMap.put(TrxCommand.class, new TrxCommandHandler(node, (TransactionKVStore)kvStore, handlerMap));

        TrxCommandHandler handler = new TrxCommandHandler(null, kvStore, handlerMap);
        List<Object> commands = new ArrayList<>();

        String key = "kkk", value = "vvvvv";
        String key1 = "bbb", value1 = "ccccc";
        commands.add(new SetCommand(key, value.getBytes()));
        commands.add(new DelCommand(key));
        commands.add(new SetCommand(key1, value1.getBytes()));

        TrxCommand trxCommand = new TrxCommand(commands);
        handler.doHandle(trxCommand);

        assertArrayEquals(null, kvStore.get(key));
        assertArrayEquals(value1.getBytes(), kvStore.get(key1));
        kvStore.close();
    }
}