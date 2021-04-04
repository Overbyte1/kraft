package server.handler;

import common.message.Response;

public interface CommandHandler {
    Response handle(Object command);
}
