package client.handler;

import client.CommandContext;

public class MGetHandler implements CommandHandler {
    @Override
    public String getCommandName() {
        return "mget";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {

    }
}
