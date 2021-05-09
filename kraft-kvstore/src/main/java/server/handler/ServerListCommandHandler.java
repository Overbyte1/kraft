package server.handler;

import common.message.response.Response;
import common.message.response.ResponseType;
import election.node.Node;
import rpc.Endpoint;
import rpc.NodeEndpoint;
import server.handler.CommandHandler;

public class ServerListCommandHandler implements CommandHandler {
    private final Node node;
    private final int portInterval;

    public ServerListCommandHandler(Node node, int portInterval) {
        this.node = node;
        this.portInterval = portInterval;
    }
    public ServerListCommandHandler(Node node) {
        this(node, 0);
    }

    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }

    @Override
    public Response doHandle(Object command) {
        NodeEndpoint[] allNodeEndpoint = node.getAllNodeEndpoint();
        NodeEndpoint[] resp = new NodeEndpoint[allNodeEndpoint.length];
        for(int i = 0; i < resp.length; i++) {
            Endpoint endpoint = allNodeEndpoint[i].getEndpoint();
            resp[i] = new NodeEndpoint(allNodeEndpoint[i].getNodeId(),
                    new Endpoint(endpoint.getIpAddress(), endpoint.getPort() + portInterval));
        }
        return new Response(ResponseType.SUCCEED,  node.getAllNodeEndpoint());
    }
}
