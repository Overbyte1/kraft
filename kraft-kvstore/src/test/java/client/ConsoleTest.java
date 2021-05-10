package client;

import client.balance.LeaderOnlyLoadBalance;
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
        endpointMap.put(new NodeId("B"), new Endpoint("101.32.214.146", 8101));

        ClientConfig config = new ClientConfigLoader().load(null);
        Console console = new Console(endpointMap, handlers, new LeaderOnlyLoadBalance(new Router(endpointMap)), config);
        console.start();
    }


    public static void main(String[] args) throws IOException {
        ConsoleTest consoleTest = new ConsoleTest();
        consoleTest.testStart();

    }

}