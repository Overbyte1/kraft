package client.handler;

import client.CommandContext;

public class SetHandler implements CommandHandler {
    @Override
    public String getCommandName() {
        return "set";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {

    }
}
