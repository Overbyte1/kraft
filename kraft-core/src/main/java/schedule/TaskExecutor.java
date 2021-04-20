package schedule;

import com.google.common.util.concurrent.FutureCallback;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface TaskExecutor {

    Future<?> submit( Runnable task);

    <V> Future<V> submit(@Nonnull Callable<V> task);
    <V> Future<?> submit(@Nonnull Callable<V> task, FutureCallback<Object> callback);
    void submit(@Nonnull Runnable task, FutureCallback<Object> callback);

    void shutdown() throws InterruptedException;

}
