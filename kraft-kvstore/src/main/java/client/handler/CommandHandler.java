package client.handler;

import client.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CommandHandler {
    Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    String getCommandName();
    void execute(String[] args, CommandContext commandContext);
}
