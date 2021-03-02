package schedule;

import java.util.concurrent.Future;

public class LogReplicationReadFuture {
    private Future<?> future;

    public LogReplicationReadFuture(Future<?> future) {
        this.future = future;
    }
    public boolean cancel() {
        return future.cancel(false);
    }
}
