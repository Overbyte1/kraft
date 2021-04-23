package client.handler;

import client.CommandContext;

public abstract class InlineCommandHandler implements CommandHandler {
    private ResponseParser responseParser = new DefaultResponseParser();

    private void showResult(Object resp, CommandHandler commandHandler, String[] args, CommandContext commandContext) {
        String result = responseParser.parse(resp, commandHandler, args, commandContext);
        System.out.println(result);
    }

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        try {
            Object resp = doExecute(args, commandContext);
            String result = responseParser.parse(resp, this, args, commandContext);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("error:" + e.getMessage());
        }
    }

    protected abstract Object doExecute(String[] args, CommandContext commandContext);
    public abstract Object getSendMessage(String[] args, CommandContext commandContext);
}
