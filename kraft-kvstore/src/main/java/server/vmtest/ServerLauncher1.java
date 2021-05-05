package server.vmtest;

import analysis.AnalysisServerLauncher;
import com.alibaba.fastjson.JSON;
import common.message.command.*;
import config.ClusterConfig;
import election.node.Node;
import election.node.NodeImpl;
import election.statemachine.DefaultStateMachine;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import server.KVDatabase;
import server.KVDatabaseImpl;
import server.config.ServerConfig;
import server.handler.*;
import server.store.KVStore;
import server.store.MemHTKVStore;
import server.store.RocksDBTransactionKVStore;
import server.store.TransactionKVStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerLauncher1 {
    private KVDatabase kvDatabase;

    private Node buildNode() throws IOException {
        File file = new File(".");
        System.out.println(file.getAbsolutePath());
        ClusterConfig config = JSON.parseObject(new FileInputStream("./kraft-kvstore/conf/raft1.json"), ClusterConfig.class);
        System.out.println(config);

        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        return builder.withId("A")
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withPath(config.getPath() + "A/")
                .withStateMachine(new DefaultStateMachine())
                .build();
    }

    private KVStore getTrxKvStore() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        KVStore transactionKVStore = new RocksDBTransactionKVStore(options, "./db/A/");
        return transactionKVStore;
    }
    private KVStore getMemKVStore() {
        return new MemHTKVStore();
    }

    public void init() throws IOException, RocksDBException {
        Node node = buildNode();
        KVStore kvStore = getTrxKvStore();
        ServerConfig config = JSON.parseObject(new FileInputStream("./kraft-kvstore/conf/server1.json"), ServerConfig.class);

        kvDatabase = new KVDatabaseImpl(node, config);

        AnalysisServerLauncher analysisServerLauncher = new AnalysisServerLauncher();
        new Thread(()->{
            analysisServerLauncher.start(kvDatabase, kvStore, node, config.getAnalysisPort());
        }).start();

//        kvDatabase.start();
//        kvDatabase.registerCommandHandler(GetCommand.class, new GetCommandHandler(kvStore));
//        kvDatabase.registerCommandHandler(SetCommand.class, new SetCommandHandler(kvStore, node));
//        kvDatabase.registerCommandHandler(DelCommand.class, new DelCommandHandler(kvStore, node));
//        kvDatabase.registerCommandHandler(MDelCommand.class, new MDelCommandHandler(kvStore, node));
//        kvDatabase.registerCommandHandler(MSetCommand.class, new MSetCommandHandler(kvStore, node));
//        kvDatabase.registerCommandHandler(MGetCommand.class, new MGetCommandHandler(kvStore));
//        kvDatabase.registerCommandHandler(LeaderCommand.class, new LeaderCommandHandler(node));
//        kvDatabase.registerCommandHandler(ServerListCommand.class, new ServerListCommandHandler(node));
//        kvDatabase.registerCommandHandler(PingCommand.class, new PingCommandHandler());

        Map<Class<?>, CommandHandler> handlerMap = new HashMap<>();
        kvDatabase.start();

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

        for (Map.Entry<Class<?>, CommandHandler> entry : handlerMap.entrySet()) {
            kvDatabase.registerCommandHandler(entry.getKey(), entry.getValue());
        }

    }
    public static void main(String[] args) throws IOException, RocksDBException {
        ServerLauncher1 launcher = new ServerLauncher1();
        launcher.init();
    }
}


