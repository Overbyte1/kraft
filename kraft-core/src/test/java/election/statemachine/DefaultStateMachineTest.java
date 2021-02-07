package election.statemachine;

import org.junit.Test;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultStateMachineTest {
    @Test
    public void testApply() {
        PriorityQueue<Integer> queue = new PriorityQueue<>();
//        queue.remove()
        StateMachine stateMachine = new DefaultStateMachine();
        stateMachine.apply();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
        ScheduledFuture<?> scheduledFuture = executorService.schedule(() -> {
        }, 1000, TimeUnit.MILLISECONDS);
    }
}