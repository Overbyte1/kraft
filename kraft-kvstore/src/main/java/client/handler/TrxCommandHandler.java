package client.handler;

import client.CommandContext;
import common.message.command.TrxCommand;
import common.message.response.FailureResult;
import common.message.response.MultiPayloadResult;
import common.message.response.Response;
import common.message.response.ResponseType;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.ArgumentCompleter;
import org.jline.reader.impl.completer.NullCompleter;
import org.jline.reader.impl.completer.StringsCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TrxCommandHandler extends InlineCommandHandler {
    private static final String ROLLBACK_COMMAND = "rollback";
    private static final String COMMIT_COMMAND = "commit";
    private boolean commit = false;
    private boolean rollback = false;
    List<String> commandNames = new ArrayList<>();
    private class Pair {
        private String name;
        private Object msg;
        public Pair(String name, Object msg) {
            this.name = name;
            this.msg = msg;
        }

        public String getName() {
            return name;
        }

        public Object getMsg() {
            return msg;
        }
    }

    @Override
    public String getCommandName() {
        return "trx";
    }

    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        commit = false;
        rollback = false;
        List<Object> commands = new ArrayList<>();
        commandNames = new ArrayList<>();

        String PROMPT = "transaction> ";
        Map<String, InlineCommandHandler> inlineCommandMap = commandContext.getInlineCommandHandlerMap();
        ArgumentCompleter completer = new ArgumentCompleter(
                new StringsCompleter(inlineCommandMap.keySet()),
                new NullCompleter()
        );
        LineReader reader = LineReaderBuilder.builder()
                .completer(completer)
                .build();
        String line;
        while (!commit && !rollback) {
            try {
                line = reader.readLine(PROMPT);
                if (line.trim().isEmpty())
                    continue;
                Pair msg = getCommandSendMessage(line, inlineCommandMap, args, commandContext);
                if(msg != null) {
                    commands.add(msg.getMsg());
                    commandNames.add(msg.getName());
                    System.out.println("queued");
                } else {
                    break;
                }
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            } catch (EndOfFileException ignored) {
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(rollback) {
            System.out.println("transaction rollback");
            return null;
        } else {
            return (Response<?>) commandContext.getLoadBalance().send(new TrxCommand(commands));
        }
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        try {
            Response<?> resp = doExecute(args, commandContext);
            if(resp == null) {
                return;
            }
            int type = resp.getType();
            if(resp.getType() == ResponseType.REDIRECT) {
                logger.info("redirect");
                execute(args, commandContext);
            } else if(type == ResponseType.FAILURE) {
                System.out.println("error: " + ((FailureResult)(resp.getBody())).getErrorMessage());
            } else {
                output(resp, commandContext);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
    }

    public void output(Response<?> response, CommandContext commandContext) {
        Response<?>[] responses = (Response<?>[]) response.getBody();
        Map<String, InlineCommandHandler> commandMap = commandContext.getInlineCommandHandlerMap();
        for(int i = 0; i < responses.length; i++) {
            String commandName = commandNames.get(i);
            commandMap.get(commandName).output(responses[i]);
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private Pair getCommandSendMessage(String line, Map<String, InlineCommandHandler> commandMap, String[] args,
                                  CommandContext commandContext) {
        String[] commandNameAndArguments = line.split("\\s+");
        String commandName = commandNameAndArguments[0];
        if(getCommandName().equals(commandName)) {
            rollback = true;
            System.out.println("does not support transaction nesting");
            return null;
        }
        if(ROLLBACK_COMMAND.equals(commandName)) {
            rollback = true;
            return null;
        }
        if(COMMIT_COMMAND.equals(commandName)) {
            commit = true;
            return null;
        }
        InlineCommandHandler command = commandMap.get(commandName);
        if (command == null) {
            logger.warn("handler of command [{}] not found", commandName);
            rollback = true;
            return null;
        }
        return new Pair(commandName, command.getSendMessage(Arrays.copyOfRange(commandNameAndArguments, 1, commandNameAndArguments.length),
                commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return null;
    }

}
