package server;

import analysis.AnalysisServerLauncher;
import com.alibaba.fastjson.JSON;
import common.codec.FrameDecoder;
import common.codec.FrameEncoder;
import common.codec.ProtocolDecoder;
import common.codec.ProtocolEncoder;
import common.message.command.*;
import config.ClusterConfig;
import config.DefaultConfigLoader;
import election.node.Node;
import election.node.NodeImpl;
import election.statemachine.DefaultStateMachine;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import server.config.ServerConfig;
import server.config.ServerConfigLoader;
import server.handler.*;
import server.store.KVStore;
import server.store.MemHTKVStore;
import server.store.RocksDBTransactionKVStore;
import server.store.TransactionKVStore;
import server.vmtest.ServerLauncher1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

public class ServerLauncher {
    private KVDatabase kvDatabase;

    private Node buildNode() throws IOException {
        File file = new File(".");
        System.out.println(file.getAbsolutePath());
        ClusterConfig config = JSON.parseObject(new FileInputStream("./conf/raft.json"), ClusterConfig.class);
        System.out.println(config);

        NodeImpl.NodeBuilder builder = NodeImpl.builder();
        return builder.withId(config.getSelfId())
                .withListenPort(config.getPort())
                .withLogReplicationInterval(config.getLogReplicationInterval())
                .withNodeList(config.getMembers())
                .withPath(config.getPath() + "/")
                .withStateMachine(new DefaultStateMachine())
                .build();
    }

    private KVStore getTrxKvStore() throws RocksDBException {
        Options options = new Options();
        options.setCreateIfMissing(true);
        KVStore transactionKVStore = new RocksDBTransactionKVStore(options, "./db/");
        return transactionKVStore;
    }
    private KVStore getMemKVStore() {
        return new MemHTKVStore();
    }

    public void init() throws IOException, RocksDBException {
        Node node = buildNode();
        KVStore kvStore = getTrxKvStore();
        ServerConfig config = JSON.parseObject(new FileInputStream("./conf/server.json"), ServerConfig.class);
        System.out.println(config);

        kvDatabase = new KVDatabaseImpl(node, config);

        AnalysisServerLauncher analysisServerLauncher = new AnalysisServerLauncher();
        //analysisServerLauncher.start(kvDatabase, kvStore, node, config.getAnalysisPort());
        new Thread(()->{
            analysisServerLauncher.start(kvDatabase, kvStore, node, config.getAnalysisPort());
        }, "analysis server thread").start();

        Map<Class<?>, CommandHandler> handlerMap = new HashMap<>();
        kvDatabase.start();

        handlerMap.put(GetCommand.class, new GetCommandHandler(kvStore));
        handlerMap.put(SetCommand.class, new SetCommandHandler(kvStore, node));
        handlerMap.put(DelCommand.class, new DelCommandHandler(kvStore, node));
        handlerMap.put(MDelCommand.class, new MDelCommandHandler(kvStore, node));
        handlerMap.put(MSetCommand.class, new MSetCommandHandler(kvStore, node));
        handlerMap.put(MGetCommand.class, new MGetCommandHandler(kvStore));
        handlerMap.put(LeaderCommand.class, new LeaderCommandHandler(node, config.getIntervalPort()));
        handlerMap.put(ServerListCommand.class, new ServerListCommandHandler(node, config.getIntervalPort()));
        handlerMap.put(PingCommand.class, new PingCommandHandler());
        handlerMap.put(TrxCommand.class, new TrxCommandHandler(node, (TransactionKVStore)kvStore, handlerMap));

        for (Map.Entry<Class<?>, CommandHandler> entry : handlerMap.entrySet()) {
            kvDatabase.registerCommandHandler(entry.getKey(), entry.getValue());
        }

    }
    public static void main(String[] args) throws IOException, RocksDBException {
        ServerLauncher launcher = new ServerLauncher();
        launcher.init();
    }
}

