package client.handler;

import client.CommandContext;

public interface ResponseParser {
    String parse(Object resp, CommandHandler commandHandler, String[] args, CommandContext commandContext);
}
