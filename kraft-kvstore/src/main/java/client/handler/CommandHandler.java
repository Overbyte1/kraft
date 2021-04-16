package client.handler;

import client.CommandContext;

public interface CommandHandler {
    String getCommandName();
    void execute(String[] args, CommandContext commandContext);
}
