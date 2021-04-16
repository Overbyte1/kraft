package client;

import client.handler.*;
import election.node.NodeId;
import org.junit.Test;
import rpc.Endpoint;

import java.util.*;

import static org.junit.Assert.*;

public class ConsoleTest {
    @Test
    public void testStart() {
        List<CommandHandler> handlers = Arrays.asList(
                new DelHandler(),
                new ExitCommand(),
                new GetHandler(),
                new LeaderHandler(),
                new MDelHandler(),
                new MGetHandler(),
                new MSetHandler(),
                new ServerListHandler(),
                new SetHandler()
        );
        Map<NodeId, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(new NodeId("B"), new Endpoint("2222222222222", 1235));
        endpointMap.put(new NodeId("A"), new Endpoint("1111111111111", 1234));
        endpointMap.put(new NodeId("C"), new Endpoint("3333333333333", 1236));
        endpointMap.put(new NodeId("D"), new Endpoint("4444444444444", 1237));

        Console console = new Console(endpointMap, handlers);
        console.start();
    }
    @Test
    public void testInput() {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        System.out.println("s = " + s);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        System.out.println("s = " + s);
    }
}