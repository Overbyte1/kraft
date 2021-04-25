package client.handler;

import client.CommandContext;
import common.message.command.GetCommand;
import common.message.response.Response;
import common.message.response.SinglePayloadResult;

public class GetHandler extends InlineCommandHandler {
    @Override
    public String getCommandName() {
        return "get";
    }

    @Override
    public Response<?> doExecute(String[] args, CommandContext commandContext) {
        Object msg = getSendMessage(args, commandContext);
        logger.debug("do get, key: {}", args[0]);
        return (Response<?>) commandContext.getLoadBalance().send(msg);
    }

    @Override
    public Object getSendMessage(String[] args, CommandContext commandContext) {
        if(args.length != 1) {
            throw new ParameterException("illegal arguments");
        }
        return new GetCommand(args[0]);
    }

    @Override
    public void output(Response<?> msg) {
        Response<SinglePayloadResult> response = (Response<SinglePayloadResult>) msg;
        SinglePayloadResult body = response.getBody();
        System.out.println(bytesToString(body.getPayload()));
    }

}
