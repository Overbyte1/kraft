package client.handler;

import client.CommandContext;
import common.message.command.GetCommand;

public class GetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "get";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        if(args.length != 1) {
            throw new ParameterException("illegal arguments");
        }
        logger.debug("do get, key: {}", args[0]);
        return commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new GetCommand(args[0]);
    }
}
