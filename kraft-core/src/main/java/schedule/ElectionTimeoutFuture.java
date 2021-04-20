package schedule;

import java.util.concurrent.Future;

public class ElectionTimeoutFuture {
    private Future<?> future;

    public ElectionTimeoutFuture(Future<?> future) {
        this.future = future;
    }
    public boolean cancel() {
        return future.cancel(false);
    }
}
