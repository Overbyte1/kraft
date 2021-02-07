package schedule;

import java.util.concurrent.*;

public class SingleTaskScheduleExecutor implements TaskScheduleExecutor {
    //TODO：合理设置线程数量
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    @Override
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    @Override
    public void shutdown() {
        executorService.shutdownNow();
    }
}
