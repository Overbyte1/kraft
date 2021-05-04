package analysis;

import common.message.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.KVListener;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThroughoutCollector implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(ThroughoutCollector.class);
    //保存每秒执行的命令数量，保留1000条数据
    private final Map<Long, Integer> map = new ConcurrentHashMap<>();
    private final LinkedList<Long> keyList = new LinkedList<>();
    private final int MAX_RECORDS_NUM = 1000;
    //private final KVDatabase kvDatabase;
    private final ThroughListener throughListener = new ThroughListener();

    public ThroughListener getThroughListener() {
        return throughListener;
    }

    @Override
    public int getType() {
        return AnalysisType.COMMAND_THROUGHOUT;
    }

    @Override
    public String collect() {
        long key = System.currentTimeMillis() / 1000;
        int n = map.getOrDefault(key, 0);
        return String.valueOf(n);
    }
    class ThroughListener implements KVListener {
        @Override
        public void listen(Connection<?> o) {
            long timeMillis = System.currentTimeMillis();
            //单位为秒
            long key = timeMillis / 1000;
            int num = map.getOrDefault(key, 0);
            logger.info("timestamp: {}, num: {}", key, num);
            if(num == 0) {
                keyList.addLast(key);
            }
            map.put(key, num + 1);
            //移除最旧的记录
            if(keyList.size() == MAX_RECORDS_NUM) {
                Long k = keyList.removeFirst();
                map.remove(k);
            }
        }
    }
}
