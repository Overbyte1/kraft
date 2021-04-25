package client.handler;

import client.CommandContext;
import common.message.command.SetCommand;
import common.message.response.Response;

public class SetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        if(args.length != 2) {
            throw new ParameterException("illegal arguments");
        }
        return new SetCommand(args[0], args[1].getBytes());
    }
    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        Object msg = getSendMessage(args, commandContext);
        logger.debug("do set, key/value: [{}]-[{}]", args[0], args[1]);
        return (Response<?>) commandContext.getLoadBalance().send(msg);
    }

}
