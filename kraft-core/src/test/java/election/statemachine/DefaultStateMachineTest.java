package election.statemachine;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class DefaultStateMachineTest {
    private int i = 0;
    private int[] arr = new int[]{0, 1};
    @Test
    public void testApply() throws InterruptedException {
        String s = "a";
        Class<?> clazz = s.getClass();
        Object obj = s;
        assert obj.getClass() == clazz;
        Node<String> stringNode = new Node("abc");
        assert stringNode.name.getClass() == String.class;
        LongAdder longAdder = null;


    }
    @Test
    public void testVolatile() throws InterruptedException {
        Tree tree = new Tree();
        Thread t1 = new Thread(()-> {
            tree.left = 9;
            while (true){}
        });

        t1.start();
        while(tree.left == 0) {

        }

        //t2.join();
    }
    @Test
    public void testSerial() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(new Integer(1));
        System.out.println(byteArrayOutputStream.size());

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
class Tree {
    int left = 0;
}