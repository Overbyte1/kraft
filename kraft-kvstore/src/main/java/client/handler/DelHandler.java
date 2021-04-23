package client.handler;

import client.CommandContext;
import common.message.command.DelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelHandler extends InlineCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelHandler.class);
    @Override
    public String getCommandName() {
        return "del";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do del");
        return commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new DelCommand(args[0]);
    }

}
