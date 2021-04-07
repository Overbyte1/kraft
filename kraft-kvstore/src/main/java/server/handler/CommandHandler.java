package server.handler;

import common.message.response.Response;

public interface CommandHandler {
    Response handleCommand(Object command);
    Response doHandle(Object command);
}
