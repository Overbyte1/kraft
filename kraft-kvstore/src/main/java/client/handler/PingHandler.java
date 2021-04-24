package client.handler;

import client.CommandContext;
import common.message.command.PingCommand;
import common.message.response.Response;

public class PingHandler extends InlineCommandHandler {
    @Override
    protected Response<?> doExecute(String[] args, CommandContext commandContext) {
        return (Response<?>) commandContext.getLoadBalance().send(new PingCommand());
    }


    @Override
    public void output(Response<?> msg) {
        System.out.println("pong");
    }

    @Override
    public String getCommandName() {
        return "ping";
    }
}
