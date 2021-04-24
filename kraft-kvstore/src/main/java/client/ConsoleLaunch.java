package client;

import client.balance.PollingLoadBalance;
import client.config.ClientConfig;
import client.config.ClientConfigLoader;
import client.handler.*;
import election.node.NodeId;
import rpc.Endpoint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

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

        Console console = null;
        try {
//            Properties properties = new Properties();
//            properties.load(ConsoleLaunch.class.getClassLoader().getResourceAsStream("./conf/client.properties"));
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            properties.store(outputStream, "");
            ClientConfig config = new ClientConfigLoader().load(null);
            console = new Console(endpointMap, handlers, new PollingLoadBalance(config, new Router(endpointMap)), config);
        } catch (IOException e) {
            System.out.println("fail to start console");
            e.printStackTrace();
        }
        console.start();
    }
}
