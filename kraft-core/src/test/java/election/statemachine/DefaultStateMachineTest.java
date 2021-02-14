package election.statemachine;

import org.junit.Test;

import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultStateMachineTest {
    private int i = 0;
    @Test
    public void testApply() throws InterruptedException {
        String s = "a";
        Class<?> clazz = s.getClass();
        Object obj = s;
        assert obj.getClass() == clazz;
        Node<String> stringNode = new Node("abc");
        assert stringNode.name.getClass() == String.class;

    }
    private void handle() {
        i = 10;
        System.out.println("i = " + i);

    }
}
class Node<T> {
    public Node(T t) {
        name = t;
    }
    T name;
}