package client;

import client.balance.PollingLoadBalance;
import client.handler.*;
import election.node.NodeId;
import rpc.Endpoint;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleLaunch {
    public static void main(String[] args) {
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
        endpointMap.put(new NodeId("B"), new Endpoint("localhost", 1235));
        endpointMap.put(new NodeId("A"), new Endpoint("1111111111111", 1234));
        endpointMap.put(new NodeId("C"), new Endpoint("3333333333333", 1236));
        endpointMap.put(new NodeId("D"), new Endpoint("4444444444444", 1237));

        Console console = new Console(endpointMap, handlers, new PollingLoadBalance(endpointMap));
        console.start();
    }
}
