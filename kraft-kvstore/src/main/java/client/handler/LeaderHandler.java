package client.handler;

import client.CommandContext;
import common.message.command.LeaderCommand;
import common.message.response.Response;
import rpc.Endpoint;
import rpc.NodeEndpoint;

public class LeaderHandler extends InlineCommandHandler {
    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do leader");
        return (Response<?>) commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new LeaderCommand();
    }

    @Override
    public void output(Response<?> msg) {
        NodeEndpoint leader = (NodeEndpoint) msg.getBody();
        if(msg.getBody() == null) {
            System.out.println("no leader, the leader has not yet been elected");
        } else {
            Endpoint endpoint = leader.getEndpoint();
            System.out.println(leader.getNodeId().getValue() + " " + endpoint.getIpAddress() + ":" + endpoint.getPort());
        }
    }


    @Override
    public String getCommandName() {
        return "leader";
    }
}
