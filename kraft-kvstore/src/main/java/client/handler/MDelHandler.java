package client.handler;

import client.CommandContext;
import common.message.command.MDelCommand;

import java.util.Arrays;

public class MDelHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mdel";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do mdel, keys: {}", Arrays.toString(args));
        return commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new MDelCommand(args);
    }
}
