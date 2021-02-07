package schedule;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SingleThreadTaskExecutor implements TaskExecutor {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <V> Future<V> submit(Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void shutdown() {
        executorService.shutdown();
    }
}
