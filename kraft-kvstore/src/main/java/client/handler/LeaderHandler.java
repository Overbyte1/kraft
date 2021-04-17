package client.handler;

import client.CommandContext;
import common.message.command.LeaderCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderHandler extends InlineCommandHandler {
    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do leader");
        return commandContext.getLoadBalance().send(new LeaderCommand());
    }

    @Override
    public String getCommandName() {
        return "leader";
    }
}
