package client;

import client.balance.PollingLoadBalance;
import client.config.ClientConfig;
import client.config.ClientConfigLoader;
import client.handler.*;
import election.node.NodeId;
import org.junit.Test;
import rpc.Endpoint;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

public class ConsoleTest {
    @Test
    public void testStart() throws IOException {
        List<CommandHandler> handlers = Arrays.asList(
                new DelHandler(),
                new ExitCommand(),
                new GetHandler(),
                new LeaderHandler(),
                new MDelHandler(),
                new MGetHandler(),
                new MSetHandler(),
                new ServerListHandler(),
                new SetHandler(),
                new PingHandler(),
                new TrxCommandHandler()
        );
        Map<NodeId, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(new NodeId("A"), new Endpoint("121.196.164.25", 8101));
//        endpointMap.put(new NodeId("B"), new Endpoint("localhost", 8102));
//        endpointMap.put(new NodeId("C"), new Endpoint("localhost", 8103));
        ClientConfig config = new ClientConfigLoader().load(null);
        Console console = new Console(endpointMap, handlers, new PollingLoadBalance(config, new Router(endpointMap)), config);
        console.start();
    }
    @Test
    public void testInput() {
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        System.out.println("s = " + s);
    }

    public static void main(String[] args) throws IOException {
        ConsoleTest consoleTest = new ConsoleTest();
        consoleTest.testStart();
        //TODO:添加允许Follower读的选项
    }

}