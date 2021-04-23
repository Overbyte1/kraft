package client.handler;

import client.CommandContext;
import common.message.command.MGetCommand;

import java.util.Arrays;

public class MGetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mget";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do mget, keys: {}", Arrays.toString(args));
        return commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new MGetCommand(args);
    }
}
