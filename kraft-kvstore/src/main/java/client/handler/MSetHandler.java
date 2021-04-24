package client.handler;

import client.CommandContext;
import common.message.command.MSetCommand;
import common.message.response.Response;

public class MSetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mset";
    }

    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {

        return (Response<?>) commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }


    public Object getSendMessage(String[] args, CommandContext commandContext) {
        if(args.length % 2 == 1 || args.length == 0) {
            throw new ParameterException("illegal arguments");
        }
        String[] keys = new String[args.length / 2];
        byte[][] value = new byte[keys.length][];

        for(int i = 0, idx = 0; i < keys.length && idx < args.length; i++, idx += 2) {
            keys[i] = args[idx];
            value[i] = args[idx + 1].getBytes();
        }
        return new MSetCommand(keys, value);
    }
}
