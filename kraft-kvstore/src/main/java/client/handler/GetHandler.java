package client.handler;

import client.CommandContext;

public class GetHandler implements CommandHandler {
    @Override
    public String getCommandName() {
        return "get";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {

    }
}
