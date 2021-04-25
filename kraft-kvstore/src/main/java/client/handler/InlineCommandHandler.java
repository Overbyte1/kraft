package client.handler;

import client.CommandContext;
import common.message.response.FailureResult;
import common.message.response.Response;
import common.message.response.ResponseType;

public abstract class InlineCommandHandler implements CommandHandler, ConsoleOutput {

    @Override
    public void execute(String[] args, CommandContext commandContext) {
        try {
            Response<?> resp = doExecute(args, commandContext);

            int type = resp.getType();
            if(resp.getType() == ResponseType.REDIRECT) {
                logger.info("redirect");
                execute(args, commandContext);
            } else if(type == ResponseType.FAILURE) {
                System.out.println("error: " + ((FailureResult)(resp.getBody())).getErrorMessage());
            } else {
                output(resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error:" + e.getMessage());
        }
    }
    //TODO:语法合法性检查
    public abstract Object getSendMessage(String[] args, CommandContext commandContext);


    @Override
    public void output(Response<?> msg) {
        System.out.println("ok");
    }

    protected abstract Response<?> doExecute(String[] args, CommandContext commandContext);

//    public abstract Object getSendMessage(String[] args, CommandContext commandContext);

    protected String bytesToString(byte[] bytes) {
        return bytes == null ? "nil" : new String(bytes);
    }

}
