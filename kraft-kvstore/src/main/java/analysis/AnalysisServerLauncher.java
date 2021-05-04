package analysis;

import server.KVDatabase;
import server.store.KVStore;

public class AnalysisServerLauncher {
    public void start(KVDatabase kvDatabase, KVStore kvStore, int port) {
        AnalysisServer server = new AnalysisServer(port);
        AllDataCollector allDataCollector = new AllDataCollector(kvStore);
        server.registerCollector(allDataCollector.getType(), allDataCollector);

        LatestCommandCollector latestCommandCollector = new LatestCommandCollector();
        server.registerCollector(latestCommandCollector.getType(), latestCommandCollector);

        ThroughoutCollector throughoutCollector = new ThroughoutCollector();
        server.registerCollector(throughoutCollector.getType(), throughoutCollector);
        kvDatabase.addAfterListener(throughoutCollector.getThroughListener());

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
