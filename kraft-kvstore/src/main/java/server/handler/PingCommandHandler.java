package server.handler;

import common.message.response.Response;
import common.message.response.ResponseType;

public class PingCommandHandler implements CommandHandler {
    @Override
    public Response handleCommand(Object command) {
        return doHandle(command);
    }

    @Override
    public Response doHandle(Object command) {
        return new Response(ResponseType.SUCCEED, null);
    }
}
