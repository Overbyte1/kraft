package client.handler;

import client.CommandContext;

public class ExitCommand implements CommandHandler {
    @Override
    public String getCommandName() {
        return "exit";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {

    }
}
