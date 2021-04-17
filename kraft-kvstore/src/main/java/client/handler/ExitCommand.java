package client.handler;

import client.CommandContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExitCommand implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ExitCommand.class);
    @Override
    public String getCommandName() {
        return "exit";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        logger.debug("do exit");
        commandContext.setRunning(false);
    }
}
