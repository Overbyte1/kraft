package client.handler;

import client.CommandContext;
import common.message.command.LeaderCommand;

public class LeaderHandler extends InlineCommandHandler {
    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do leader");
        return commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new LeaderCommand();
    }

    @Override
    public String getCommandName() {
        return "leader";
    }
}
