package client.handler;

import client.CommandContext;

public class ServerListHandler implements CommandHandler {
    @Override
    public String getCommandName() {
        return "serverlist";
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
    }
}
