package client;

import client.balance.LoadBalance;
import client.config.ClientConfig;
import client.config.ClientConfigLoader;
import client.handler.CommandHandler;
import election.node.NodeId;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.io.IOException;
import java.util.*;

public class Console {
    private static final Logger logger = LoggerFactory.getLogger(Console.class);

    private static final String PROMPT = "kvstore-client " + Client.VERSION + "> ";
    private final Map<String, CommandHandler> commandMap;
    private final LineReader reader;
    private final CommandContext commandContext;

    public Console(Map<NodeId, Endpoint> serverMap, List<CommandHandler> handlerList, LoadBalance loadBalance) throws IOException {
        this(serverMap, handlerList, loadBalance, new ClientConfigLoader().load(null));
    }

    public Console(Map<NodeId, Endpoint> serverMap, List<CommandHandler> handlerList, LoadBalance loadBalance, ClientConfig config) {
        commandMap = buildCommandMap(handlerList);

        commandContext = new CommandContext(serverMap, loadBalance, config);

        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(commandMap.keySet()),
                new NullCompleter()
        );
        reader = LineReaderBuilder.builder()
                .completer(completer)
                .build();
    }

    private Map<String, CommandHandler> buildCommandMap(Collection<CommandHandler> commands) {
        Map<String, CommandHandler> commandMap = new HashMap<>();
        for (CommandHandler cmd : commands) {
            commandMap.put(cmd.getCommandName(), cmd);
        }
        return commandMap;
    }

    void start() {
        showInfo();
        commandContext.setRunning(true);
        String line = null;
        logger.debug("start console.");
        while (commandContext.isRunning()) {
            try {
                line = reader.readLine(PROMPT);
                if (line.trim().isEmpty())
                    continue;
                dispatchCommand(line);
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } catch (EndOfFileException ignored) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showInfo() {
        System.out.println("Welcome to KVStore Shell\n");
        System.out.println("***********************************************");
        System.out.println("current server list: \n");
//        commandContext.printSeverList();
        printServerList();
        System.out.println("***********************************************");
    }
    private void printServerList() {
        Router router = commandContext.getRouter();
        List<NodeEndpoint> serverList = router.getServerList();
        boolean hasLeader = router.hasLeader();
        NodeId nodeId = router.getLeaderId();
        serverList.sort(Comparator.comparing(NodeEndpoint::getNodeId));
        for (NodeEndpoint nodeEndpoint : serverList) {
            System.out.print(nodeEndpoint.getNodeId() + " " + nodeEndpoint.getEndpoint());
            if(hasLeader && nodeId.equals(nodeEndpoint.getNodeId())) {
                System.out.print(" (leader)");
            }
            System.out.println();
        }
    }

    public void dispatchCommand(String line) {
        String[] commandNameAndArguments = line.split("\\s+");
        String commandName = commandNameAndArguments[0];
        CommandHandler command = commandMap.get(commandName);
        logger.debug("input command: {}", commandName);
        if (command == null) {
            logger.warn("handler of command [{}] not found", commandName);
            throw new IllegalArgumentException("no such command [" + commandName + "]");
        }
        command.execute(Arrays.copyOfRange(commandNameAndArguments, 1, commandNameAndArguments.length), commandContext);
        //command.execute(commandNameAndArguments.length > 1 ? commandNameAndArguments[1] : "", commandContext);
    }
}
