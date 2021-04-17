package client.handler;

import client.CommandContext;
import common.message.command.SetCommand;

public class SetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        if(args.length != 2) {
            throw new ParameterException("illegal arguments");
        }
        logger.debug("do set, key/value: [{}]-[{}]", args[0], args[1]);
        return commandContext.getLoadBalance().send(new SetCommand(args[0], args[1].getBytes()));
    }
}
