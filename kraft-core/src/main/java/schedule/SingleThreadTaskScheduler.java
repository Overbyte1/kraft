package schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleThreadTaskScheduler implements TaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadTaskScheduler.class);
    //TODO:配置类
    private int minElectionTimeout = 6000;
    private int maxElectionTimeout = 10000;
    private int logReplicationInterval = 3000;
    private int logReplicationResultTimeout = 1500;
    private int connectTimeout = 800;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private Random random = new Random();

    @Override
    public ElectionTimeoutFuture scheduleElectionTimeoutTask(Runnable task) {
        logger.info("election timeout task was commit");
        int timeout = random.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(task, timeout, timeout, timeUnit);
        return new ElectionTimeoutFuture(scheduledFuture);
    }

    @Override
    public LogReplicationFuture scheduleLogReplicationTask(Runnable task) {
        logger.info("log replication timeout task was commit");
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(task, 0, logReplicationInterval, timeUnit);
        return new LogReplicationFuture(scheduledFuture);

    }

    @Override
    public LogReplicationReadFuture scheduleLogReplicationReadTask(Runnable task) {
        logger.info("log replication read timeout task was commit");
        ScheduledFuture<?> schedule = scheduledExecutorService.schedule(task, logReplicationResultTimeout, timeUnit);
        return new LogReplicationReadFuture(schedule);
    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdownNow();
    }
}
