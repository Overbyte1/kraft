package client.handler;

import client.CommandContext;
import common.message.command.MDelCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MDelHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "mdel";
    }

    @Override
    public Object doExecute(String[] args, CommandContext commandContext) {
        logger.debug("do mdel, keys: {}", Arrays.toString(args));
        return commandContext.getLoadBalance().send(new MDelCommand(args));
    }
}
