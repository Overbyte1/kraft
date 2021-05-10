package server.handler;

import common.message.response.FailureResult;
import common.message.response.Response;
import common.message.response.ResponseType;
import election.node.Node;
import rpc.Endpoint;
import rpc.NodeEndpoint;

public class LeaderCommandHandler implements CommandHandler {
    private final Node node;
    private final int portInterval;

    public LeaderCommandHandler(Node node, int portInterval) {
        this.node = node;
        this.portInterval = portInterval;
    }

    public LeaderCommandHandler(Node node) {
        this(node, 0);
    }

    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }

    @Override
    public Response doHandle(Object command) {
        NodeEndpoint nodeEndpoint = node.getLeaderNodeEndpoint();
        if(nodeEndpoint == null) {
            return new Response(ResponseType.SUCCEED, null);
        }
        Endpoint endpoint = nodeEndpoint.getEndpoint();
        return new Response(ResponseType.SUCCEED, new NodeEndpoint(nodeEndpoint.getNodeId(),
                new Endpoint(endpoint.getIpAddress(), endpoint.getPort() + portInterval)));
    }
}
