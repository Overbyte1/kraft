package server.handler;

import common.message.response.Response;
import common.message.response.ResponseType;
import election.node.Node;
import server.handler.CommandHandler;

public class ServerListCommandHandler implements CommandHandler {
    private final Node node;

    public ServerListCommandHandler(Node node) {
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }

    @Override
    public Response doHandle(Object command) {
        return new Response(ResponseType.SUCCEED,  node.getAllNodeEndpoint());
    }
}
