package analysis;

import election.node.Node;
import server.KVDatabase;
import server.store.KVStore;

public class AnalysisServerLauncher {
    public void start(KVDatabase kvDatabase, KVStore kvStore, Node node, int port) {
        AnalysisServer server = new AnalysisServer(port);
        //所有的Key/Value数据
        AllDataCollector allDataCollector = new AllDataCollector(kvStore);
        server.registerCollector(allDataCollector.getType(), allDataCollector);

        LatestCommandCollector latestCommandCollector = new LatestCommandCollector();
        server.registerCollector(latestCommandCollector.getType(), latestCommandCollector);
        //吞吐量，每秒执行的任务数量
        ThroughoutCollector throughoutCollector = new ThroughoutCollector();
        server.registerCollector(throughoutCollector.getType(), throughoutCollector);
        kvDatabase.addAfterListener(throughoutCollector.getThroughListener());
        //当前角色，Leader、Candidate或者Follower之一
        RoleTypeCollector roleTypeCollector = new RoleTypeCollector(node);
        server.registerCollector(roleTypeCollector.getType(), roleTypeCollector);

        TermCollector termCollector = new TermCollector(node);
        server.registerCollector(termCollector.getType(), termCollector);

        server.start();
    }

//    public static void main(String[] args) throws RocksDBException {
//        int port = 9981;
//        AnalysisServer server = new AnalysisServer(port);
//        Options options = new Options();
//        options.setCreateIfMissing(true);
//        KVStore kvStore = new RocksDBTransactionKVStore(options, "./db/A/");
//        AllDataCollector allDataCollector = new AllDataCollector(kvStore);
//        server.registerCollector(allDataCollector.getType(), allDataCollector);
//
//        LatestCommandCollector latestCommandCollector = new LatestCommandCollector();
//        server.registerCollector(latestCommandCollector.getType(), latestCommandCollector);
//
//        ThroughoutCollector throughoutCollector = new ThroughoutCollector();
//        server.registerCollector(throughoutCollector.getType(), throughoutCollector);
//
//        server.start();
//
//    }
}
