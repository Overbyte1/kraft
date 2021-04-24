package client.handler;

import client.CommandContext;
import common.message.command.LeaderCommand;
import common.message.response.Response;

public class LeaderHandler extends InlineCommandHandler {
    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do leader");
        return (Response<?>) commandContext.getLoadBalance().send(new LeaderCommand());
    }

    @Override
    public void output(Response<?> msg) {
        System.out.println(msg.getBody());
    }


    @Override
    public String getCommandName() {
        return "leader";
    }
}
