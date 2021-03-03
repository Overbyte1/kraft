package schedule;

import java.util.concurrent.Future;

public class LogReplicationFuture {
    private Future<?> future;

    public LogReplicationFuture(Future<?> future) {
        this.future = future;
    }
    public boolean cancel() {
        System.out.println("LogReplicationFuture cancel");
        return future.cancel(false);
    }
}
