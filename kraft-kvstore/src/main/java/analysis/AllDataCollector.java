package analysis;

import server.store.KVStore;
import server.store.KVStoreIterator;

import java.io.IOException;

public class AllDataCollector implements Collector {
    private final KVStore kvStore;

    public AllDataCollector(KVStore kvStore) {
        this.kvStore = kvStore;
    }

    @Override
    public int getType() {
        return AnalysisType.ALL_KV_DATA;
    }

    @Override
    public String collect() {

        StringBuilder sb = new StringBuilder();
        String gap = "\t\t\t\t\t\t\t";
        sb.append("key").append(gap).append("value").append("\n");
        sb.append("-----------------------------------------------------\n");
        try (final KVStoreIterator iterator = kvStore.newIterator()) {
            iterator.seekToFirst();
            for (; iterator.isValid(); iterator.next()) {
                sb.append(new String(iterator.key()))
                        .append(gap)
                        .append(new String(iterator.value()))
                        .append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //            Map<byte[], byte[]> map = (Map<byte[], byte[]>) handler.getMsg();
//            String gap = "\t\t\t\t\t\t\t";
//            System.out.println("key" + gap + "value");
//            System.out.println("-----------------------------------------------------");
//            for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
//                System.out.println(new String(entry.getKey()) + gap + new String(entry.getValue()));
//            }
        return sb.toString();
    }
}
