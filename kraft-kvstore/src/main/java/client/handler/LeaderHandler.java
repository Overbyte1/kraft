package client.handler;

import client.CommandContext;

public class LeaderHandler implements CommandHandler {
    @Override
    public void execute(String[] args, CommandContext commandContext) {

    }

    @Override
    public String getCommandName() {
        return "leader";
    }
}
