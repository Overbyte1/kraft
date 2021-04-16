package client.handler;

import client.CommandContext;
import common.message.command.DelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelHandler implements CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DelHandler.class);
    @Override
    public String getCommandName() {
        return "del";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        logger.debug("do del");
        try {
            Object resp = commandContext.getLoadBalance().send(new DelCommand(args[0]));

        } catch (Exception exception) {
            //exception.printStackTrace();
            System.out.println("timeout");
        }
    }
}
