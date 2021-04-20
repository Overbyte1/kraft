package schedule;

import config.ClusterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SingleThreadTaskScheduler implements TaskScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SingleThreadTaskScheduler.class);

    //private final ClusterConfig config;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private final Random random = new Random();

    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    private int minElectionTimeout;
    private int maxElectionTimeout;
    private int logReplicationTimeout;

    public SingleThreadTaskScheduler(int minElectionTimeout, int maxElectionTimeout, int logReplicationTimeout) {
        this.minElectionTimeout = minElectionTimeout;
        this.maxElectionTimeout = maxElectionTimeout;
        this.logReplicationTimeout = logReplicationTimeout;
    }

    @Override
    public ElectionTimeoutFuture scheduleElectionTimeoutTask(Runnable task) {
        logger.debug("election timeout task was commit");

        int timeout = random.nextInt(maxElectionTimeout - minElectionTimeout)
                    + minElectionTimeout;
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.schedule(task, timeout, timeUnit);
        return new ElectionTimeoutFuture(scheduledFuture);
    }

    @Override
    public LogReplicationFuture scheduleLogReplicationTask(Runnable task) {
        logger.debug("log replication timeout task was commit");
        ScheduledFuture<?> scheduledFuture =
                scheduledExecutorService.scheduleWithFixedDelay(task, logReplicationTimeout, logReplicationTimeout, timeUnit);
        return new LogReplicationFuture(scheduledFuture);

    }
//
//    @Override
//    public LogReplicationReadFuture scheduleLogReplicationReadTask(Runnable task) {
//        //logger.info("log replication read timeout task was commit");
//        System.out.println("log replication read timeout task was commit");
//        int timeout = random.nextInt(maxElectionTimeout - minElectionTimeout) + minElectionTimeout;
//        ScheduledFuture<?> scheduledFuture =
//                scheduledExecutorService.schedule(task, timeout, timeUnit);
//        ScheduledFuture<?> schedule = scheduledExecutorService.schedule(task, timeout, timeUnit);
//        return new LogReplicationReadFuture(schedule);
//    }

    @Override
    public void stop() {
        scheduledExecutorService.shutdownNow();
    }
}
