package schedule;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {
    Future<?> submit(Runnable task);

    //TODO：泛型使用
    <V> Future<V> submit(Callable<V> task);

    void shutdown();
}
