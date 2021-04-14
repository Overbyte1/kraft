package client;

import client.handler.CommandHandler;
import election.node.NodeId;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.*;

public class Console {
    private static final String PROMPT = "kvstore-client " + Client.VERSION + "> ";
    private final Map<String, CommandHandler> commandMap;
    //private final CommandContext commandContext;
    private final Router router;
    private final LineReader reader;
    private boolean running = false;

    public Console(Map<NodeId, Endpoint> serverMap, List<CommandHandler> handlerList) {
        commandMap = buildCommandMap(handlerList);
//        commandMap = buildCommandMap(Arrays.asList(
//                new ExitCommand(),
//                new ClientAddServerCommand(),
//                new ClientRemoveServerCommand(),
//                new ClientListServerCommand(),
//                new ClientGetLeaderCommand(),
//                new ClientSetLeaderCommand(),
//                new RaftAddNodeCommand(),
//                new RaftRemoveNodeCommand(),
//                new KVStoreGetCommand(),
//                new KVStoreSetCommand()
//        ));
        router = new Router(serverMap);
        //commandContext = new CommandContext(serverMap);

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
        running = true;
        showInfo();
        String line;
        while (running) {
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
        System.out.println("Welcome to XRaft KVStore Shell\n");
        System.out.println("***********************************************");
        System.out.println("current server list: \n");
//        commandContext.printSeverList();
        printServerList();
        System.out.println("***********************************************");
    }
    private void printServerList() {
        List<NodeEndpoint> serverList = router.getServerList();
        boolean hasLeader = router.hasLeader();
        NodeId nodeId = router.getLeaderId();
        serverList.sort(Comparator.comparing(NodeEndpoint::getNodeId));
        for (NodeEndpoint nodeEndpoint : serverList) {
            System.out.print(nodeEndpoint.getNodeId() + " " + nodeEndpoint.getEndpoint());

        }
    }

    private void dispatchCommand(String line) {
        String[] commandNameAndArguments = line.split("\\s+", 2);
        String commandName = commandNameAndArguments[0];
        CommandHandler command = commandMap.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("no such command [" + commandName + "]");
        }
        command.execute(commandNameAndArguments);
        //command.execute(commandNameAndArguments.length > 1 ? commandNameAndArguments[1] : "", commandContext);
    }
}
