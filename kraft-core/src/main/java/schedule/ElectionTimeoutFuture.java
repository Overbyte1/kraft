package schedule;

import java.util.concurrent.Future;

//TODO：选举超时、定时任务如何设计？
public class ElectionTimeoutFuture {
    private Future<?> future;

    public ElectionTimeoutFuture(Future<?> future) {
        this.future = future;
    }
    public boolean cancel() {
        System.out.println("ElectionTimeoutFuture cancel");
        return future.cancel(false);
    }
}
