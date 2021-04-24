package client.handler;

import common.message.response.Response;

public interface ConsoleOutput {
    void output(Response<?> msg);
}
