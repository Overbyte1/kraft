package schedule;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface TaskScheduleExecutor {
    ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit);

    <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit timeUnit);



    void shutdown();
}
