package client.handler;

import client.CommandContext;
import common.message.command.ServerListCommand;
import common.message.response.Response;
import common.message.response.ResponseType;
import rpc.Endpoint;
import rpc.NodeEndpoint;

import java.util.Arrays;
import java.util.Comparator;

public class ServerListHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "serverlist";
    }


    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        return new ServerListCommand();
    }

    @Override
    public void output(Response<?> msg) {
        NodeEndpoint[] nodeEndpoints = (NodeEndpoint[])(msg.getBody());
        Arrays.sort(nodeEndpoints, Comparator.comparing(NodeEndpoint::getNodeId));
        for (NodeEndpoint nodeEndpoint : nodeEndpoints) {
            Endpoint endpoint = nodeEndpoint.getEndpoint();
            System.out.println(nodeEndpoint.getNodeId().getValue() + " " + endpoint.getIpAddress() + ":" + endpoint.getPort());
        }
    }

    @Override
    protected Response<?> doExecute(String[] args, CommandContext commandContext) {
        Response<?> response =  (Response<?>) commandContext.getLoadBalance().send(getSendMessage(args, commandContext));
        if(response.getType() == ResponseType.SUCCEED) {
            NodeEndpoint[] nodeEndpoints = (NodeEndpoint[])(response.getBody());
            commandContext.getLoadBalance().initRouter(nodeEndpoints);
        }
        return response;
    }

}
