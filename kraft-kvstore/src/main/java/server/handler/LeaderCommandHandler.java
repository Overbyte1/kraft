package server.handler;

import common.message.response.Response;
import common.message.response.ResponseType;
import election.node.Node;

public class LeaderCommandHandler implements CommandHandler {
    private final Node node;

    public LeaderCommandHandler(Node node) {
        this.node = node;
    }

    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }

    @Override
    public Response doHandle(Object command) {

        return new Response(ResponseType.SUCCEED, node.getLeaderNodeEndpoint());
    }
}
