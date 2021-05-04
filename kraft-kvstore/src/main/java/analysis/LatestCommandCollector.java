package analysis;

import common.message.Connection;
import server.KVListener;

public class LatestCommandCollector implements Collector {
    @Override
    public int getType() {
        return AnalysisType.LATEST_COMMAND;
    }

    @Override
    public String collect() {
        return "latest command";
    }
    class LatestCommand implements KVListener {
        @Override
        public void listen(Connection<?> o) {
            Object command = o.getCommand();
            System.out.println(command);
        }
    }

}
