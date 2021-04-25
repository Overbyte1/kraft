package client.handler;

import client.CommandContext;
import common.message.command.MDelCommand;
import common.message.response.Response;

import java.util.Arrays;

public class MDelHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mdel";
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        if(args.length <= 1) {
            throw new ParameterException("illegal arguments");
        }
        return new MDelCommand(args);
    }

    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do mdel, keys: {}", Arrays.toString(args));
        return (Response<?>) commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

}
