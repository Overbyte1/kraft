package client.handler;

import client.CommandContext;
import common.message.command.MSetCommand;

public class MSetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mset";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        if(args.length % 2 == 1 || args.length == 0) {
            throw new ParameterException("illegal arguments");
        }
        String[] keys = new String[args.length / 2];
        byte[][] value = new byte[keys.length][];

        for(int i = 0, idx = 0; i < keys.length && idx < args.length; i++, idx += 2) {
            keys[i] = args[idx];
            value[i] = args[idx + 1].getBytes();
        }
        return commandContext.getLoadBalance().send(new MSetCommand(keys, value));
    }
}
