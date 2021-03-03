package schedule;

public interface TaskScheduler {
    ElectionTimeoutFuture scheduleElectionTimeoutTask(Runnable task);

    LogReplicationFuture scheduleLogReplicationTask(Runnable task);

    //LogReplicationReadFuture scheduleLogReplicationReadTask(Runnable task);

    void stop();
}
