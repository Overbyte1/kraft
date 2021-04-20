package schedule;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
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
    public <V> Future<V> submit(@Nonnull Callable<V> task, FutureCallback<Object> callback) {
        Callable<V> tempTask = ()->{
            V res = null;
            try {
                res = task.call();
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onFailure(e);
            }
            return res;
        };
        return executorService.submit(tempTask);
    }
    @Override
    public void submit(@Nonnull Runnable task, FutureCallback<Object> callback) {
        Runnable tempTask = ()->{
            try {
                task.run();
                callback.onSuccess(null);
            } catch (Exception e) {
                callback.onFailure(e);
            }

        };
        executorService.execute(tempTask);
    }

    @Override
    public <V> Future<V> submit(@Nonnull Callable<V> task) {
        return executorService.submit(task);
    }

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
    }
}
