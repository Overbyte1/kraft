package analysis;

import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import server.store.KVStore;
import server.store.RocksDBTransactionKVStore;

public class AnalysisServerLauncher {
    public static void main(String[] args) throws RocksDBException {
        int port = 9981;
        AnalysisServer server = new AnalysisServer(port);
        Options options = new Options();
        options.setCreateIfMissing(true);
        KVStore kvStore = new RocksDBTransactionKVStore(options, "./db/A/");
        AllDataCollector allDataCollector = new AllDataCollector(kvStore);
        server.registerCollector(allDataCollector.getType(), allDataCollector);

        LatestCommandCollector latestCommandCollector = new LatestCommandCollector();
        server.registerCollector(latestCommandCollector.getType(), latestCommandCollector);

        ThroughoutCollector throughoutCollector = new ThroughoutCollector();
        server.registerCollector(throughoutCollector.getType(), throughoutCollector);

        server.start();

    }
}
